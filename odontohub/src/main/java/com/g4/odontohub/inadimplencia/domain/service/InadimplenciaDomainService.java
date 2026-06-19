package com.g4.odontohub.inadimplencia.domain.service;

import com.g4.odontohub.inadimplencia.domain.event.*;
import com.g4.odontohub.inadimplencia.domain.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class InadimplenciaDomainService {

    // ── Repositórios em memória ───────────────────────────────────────────────

    private final Map<String, Paciente>          pacientes   = new HashMap<>();
    private final Map<String, Parcela>           parcelas    = new HashMap<>();
    private final Map<String, Acordo>            acordos     = new HashMap<>();
    private final Map<String, TentativaCobranca> tentativas  = new HashMap<>();
    private final List<Object>                   domainEvents = new ArrayList<>();

    // ── Inadimplência — consulta ──────────────────────────────────────────────

    public List<ResumoInadimplencia> consultarInadimplentes() {
        // Garante que parcelas estejam marcadas como vencidas
        parcelas.values().forEach(Parcela::verificarEMarcarVencida);

        List<ResumoInadimplencia> resultado = new ArrayList<>();

        Map<String, List<Parcela>> porPaciente = parcelas.values().stream()
                .filter(Parcela::isVencida)
                .collect(Collectors.groupingBy(Parcela::getPacienteId));

        for (Map.Entry<String, List<Parcela>> entry : porPaciente.entrySet()) {
            String pacienteId = entry.getKey();
            Paciente paciente = pacientes.get(pacienteId);
            if (paciente == null) continue;

            boolean semCobrancaRecente = tentativas.values().stream()
                    .filter(t -> t.getPacienteId().equals(pacienteId))
                    .noneMatch(t -> t.isRecente(15));

            for (Parcela p : entry.getValue()) {
                resultado.add(new ResumoInadimplencia(paciente, p, semCobrancaRecente));
            }
        }
        return resultado;
    }

    // ── Status Restrito ───────────────────────────────────────────────────────

    public void atualizarSituacaoFinanceira(String pacienteId) {
        parcelas.values().forEach(Parcela::verificarEMarcarVencida);

        Paciente paciente = buscarPaciente(pacienteId);

        boolean possuiParcelaVencida = parcelas.values().stream()
                .filter(p -> p.getPacienteId().equals(pacienteId))
                .anyMatch(p -> p.isVencida() && p.getDiasAtraso() >= 30);

        boolean possuiAcordoInadimplido = acordos.values().stream()
                .filter(a -> a.getPacienteId().equals(pacienteId))
                .anyMatch(a -> a.getStatus() == StatusAcordo.INADIMPLIDO);

        if (possuiParcelaVencida || possuiAcordoInadimplido) {
            if (!paciente.isRestrito()) {
                paciente.aplicarRestricao();
                domainEvents.add(new PacienteRestritoEvent(pacienteId));
            }
        } else {
            if (paciente.isRestrito()) {
                paciente.removerRestricao();
                domainEvents.add(new RestricaoRemovidaEvent(pacienteId));
            }
        }

        // Verifica acordos ativos com parcelas vencidas → inadimplir
        acordos.values().stream()
                .filter(a -> a.getPacienteId().equals(pacienteId))
                .filter(a -> a.getStatus() == StatusAcordo.ATIVO)
                .filter(Acordo::possuiParcelaVencida)
                .forEach(a -> {
                    a.marcarInadimplido("SISTEMA", "Parcela do acordo vencida");
                    domainEvents.add(new AcordoInadimplidoEvent(a.getId(), pacienteId));
                    if (!paciente.isRestrito()) {
                        paciente.aplicarRestricao();
                        domainEvents.add(new PacienteRestritoEvent(pacienteId));
                    }
                });
    }

    public void quitarUltimaPendencia(String pacienteId) {
        Paciente paciente = buscarPaciente(pacienteId);
        // Marca todas parcelas como pagas e remove restrição
        parcelas.values().stream()
                .filter(p -> p.getPacienteId().equals(pacienteId))
                .filter(p -> p.getStatus() != StatusParcela.PAGA)
                .forEach(Parcela::pagar);

        if (paciente.isRestrito()) {
            paciente.removerRestricao();
            domainEvents.add(new RestricaoRemovidaEvent(pacienteId));
        }
    }

    // ── Bloqueio de agendamento ───────────────────────────────────────────────

    public void validarAgendamento(String pacienteId) {
        Paciente paciente = buscarPaciente(pacienteId);
        if (paciente.isRestrito()) {
            throw new IllegalStateException("Paciente possui restrição financeira");
        }
    }

    // ── Tentativa de cobrança ─────────────────────────────────────────────────

    public TentativaCobranca registrarTentativaCobranca(String pacienteId, String responsavel,
                                                         CanalCobranca canal,
                                                         String resultado, String observacao) {
        TentativaCobranca t = new TentativaCobranca(pacienteId, responsavel, canal, resultado, observacao);
        tentativas.put(t.getId(), t);
        return t;
    }

    // ── Criação de acordo ─────────────────────────────────────────────────────

    public Acordo criarAcordo(String pacienteId, int numeroParcelas, String justificativa) {
        parcelas.values().forEach(Parcela::verificarEMarcarVencida);

        List<Parcela> vencidas = parcelas.values().stream()
                .filter(p -> p.getPacienteId().equals(pacienteId))
                .filter(Parcela::isVencida)
                .collect(Collectors.toList());

        if (vencidas.isEmpty()) {
            throw new IllegalStateException("É necessário possuir ao menos uma parcela vencida");
        }

        boolean acordoAtivo = acordos.values().stream()
                .filter(a -> a.getPacienteId().equals(pacienteId))
                .anyMatch(a -> a.getStatus() == StatusAcordo.ATIVO);

        if (acordoAtivo) {
            throw new IllegalStateException("Já existe um acordo ativo para este paciente");
        }

        // Soma valor total com encargos já calculados (bloqueado para edição manual)
        BigDecimal valorTotal = vencidas.stream()
                .map(Parcela::getValorAtualizado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<String> idsOriginais = vencidas.stream()
                .map(Parcela::getId)
                .collect(Collectors.toList());

        Acordo acordo = new Acordo(pacienteId, valorTotal, numeroParcelas, justificativa, idsOriginais);

        // Substitui parcelas originais
        vencidas.forEach(Parcela::substituir);

        // Gera novas parcelas
        List<Parcela> novasParcelas = acordo.gerarParcelas(pacienteId);
        novasParcelas.forEach(p -> parcelas.put(p.getId(), p));

        acordos.put(acordo.getId(), acordo);
        domainEvents.add(new AcordoCriadoEvent(acordo.getId(), pacienteId));

        return acordo;
    }

    // ── Cancelamento de acordo ────────────────────────────────────────────────

    public void cancelarAcordo(String acordoId, String responsavel, String justificativa) {
        Acordo acordo = buscarAcordo(acordoId);

        // Cancela parcelas geradas pelo acordo
        acordo.getParcelasGeradas().forEach(p -> {
            parcelas.get(p.getId()).cancelar();
        });

        // Reativa parcelas originais como vencidas e recalcula encargos (já calculados pelo modelo)
        acordo.getParcelasOriginaisIds().forEach(id -> {
            Parcela original = parcelas.get(id);
            if (original != null) {
                original.reativarComoVencida();
            }
        });

        acordo.cancelar(responsavel, justificativa);
        domainEvents.add(new AcordoCanceladoEvent(acordoId, acordo.getPacienteId(), justificativa));
    }

    // ── Histórico de negociação ───────────────────────────────────────────────

    public void alterarStatusAcordo(String acordoId, StatusAcordo novoStatus,
                                     String responsavel, String justificativa) {
        Acordo acordo = buscarAcordo(acordoId);
        StatusAcordo anterior = acordo.getStatus();
        acordo.registrarAlteracaoHistorico(responsavel, anterior, novoStatus, justificativa);
    }

    // ── Encaminhamento para F17 ───────────────────────────────────────────────

    public EncaminhamentoPagamentoEvent encaminharParaPagamento(String parcelaId) {
        Parcela parcela = buscarParcela(parcelaId);
        EncaminhamentoPagamentoEvent event =
                new EncaminhamentoPagamentoEvent(parcelaId, parcela.getPacienteId());
        domainEvents.add(event);
        return event;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Paciente buscarPaciente(String pacienteId) {
        Paciente p = pacientes.get(pacienteId);
        if (p == null) throw new NoSuchElementException("Paciente não encontrado: " + pacienteId);
        return p;
    }

    private Parcela buscarParcela(String parcelaId) {
        Parcela p = parcelas.get(parcelaId);
        if (p == null) throw new NoSuchElementException("Parcela não encontrada: " + parcelaId);
        return p;
    }

    private Acordo buscarAcordo(String acordoId) {
        Acordo a = acordos.get(acordoId);
        if (a == null) throw new NoSuchElementException("Acordo não encontrado: " + acordoId);
        return a;
    }

    // ── Acesso aos repositórios (usado pelos Steps e ApplicationService) ───────

    public void adicionarPaciente(Paciente p)  { pacientes.put(p.getId(), p); }
    public void adicionarParcela(Parcela p)    { parcelas.put(p.getId(), p); }
    public void adicionarAcordo(Acordo a)      { acordos.put(a.getId(), a); }

    public Paciente buscarPacientePorNome(String nome) {
        return pacientes.values().stream()
                .filter(p -> p.getNome().equals(nome))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Paciente não encontrado: " + nome));
    }

    public List<Parcela> listarParcelasDoPaciente(String pacienteId) {
        return parcelas.values().stream()
                .filter(p -> p.getPacienteId().equals(pacienteId))
                .collect(Collectors.toList());
    }

    public Optional<Acordo> buscarAcordoAtivoDoPaciente(String pacienteId) {
        return acordos.values().stream()
                .filter(a -> a.getPacienteId().equals(pacienteId))
                .filter(a -> a.getStatus() == StatusAcordo.ATIVO)
                .findFirst();
    }

    public List<TentativaCobranca> listarTentativasDoPaciente(String pacienteId) {
        return tentativas.values().stream()
                .filter(t -> t.getPacienteId().equals(pacienteId))
                .collect(Collectors.toList());
    }

    public List<Object> getDomainEvents() { return domainEvents; }
    public Map<String, Parcela> getParcelas() { return parcelas; }
    public Map<String, Acordo> getAcordos() { return acordos; }
}
