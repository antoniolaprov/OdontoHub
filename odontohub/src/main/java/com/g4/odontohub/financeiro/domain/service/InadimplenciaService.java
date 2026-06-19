package com.g4.odontohub.financeiro.domain.service;

import com.g4.odontohub.financeiro.domain.event.AcordoFirmado;
import com.g4.odontohub.financeiro.domain.event.AcordoInadimplido;
import com.g4.odontohub.financeiro.domain.model.Acordo;
import com.g4.odontohub.financeiro.domain.model.AcordoId;
import com.g4.odontohub.financeiro.domain.model.ContatoCobranca;
import com.g4.odontohub.financeiro.domain.model.HistoricoNegociacao;
import com.g4.odontohub.financeiro.domain.model.ParcelaCobranca;
import com.g4.odontohub.financeiro.domain.model.StatusAcordo;
import com.g4.odontohub.financeiro.domain.model.StatusParcela;
import com.g4.odontohub.financeiro.domain.repository.InadimplenciaRepository;
import com.g4.odontohub.shared.DomainEventPublisher;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InadimplenciaService {

    private static final int DIAS_PARA_RESTRICAO = 30;
    private static final double PERCENTUAL_MULTA = 0.02;
    private static final double PERCENTUAL_JUROS_DIA = 0.0033;
    private static final String RESPONSAVEL_PADRAO = "Recepcionista";

    private final InadimplenciaRepository repositorio;

    public InadimplenciaService(InadimplenciaRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void registrarPaciente(String paciente) {
        repositorio.registrarPaciente(paciente);
    }

    public ParcelaCobranca registrarParcelaVencida(String paciente, double valor, int diasAtraso) {
        ParcelaCobranca parcela = novaParcelaVencida(valor, diasAtraso);
        repositorio.salvarVencida(paciente, parcela);
        return parcela;
    }

    public void registrarParcelasVencidas(String paciente, int quantidade, double valorTotal) {
        double valorCada = valorTotal / quantidade;
        for (int i = 0; i < quantidade; i++) {
            registrarParcelaVencida(paciente, valorCada, 31);
        }
    }

    public List<ParcelaCobranca> consultarInadimplentes() {
        return repositorio.pacientes().stream()
                .flatMap(p -> repositorio.vencidasDe(p).stream())
                .filter(p -> p.getStatus() == StatusParcela.VENCIDA)
                .toList();
    }

    public boolean verificarRestricao(String paciente) {
        return repositorio.vencidasDe(paciente).stream().anyMatch(p -> p.getDiasAtraso() > DIAS_PARA_RESTRICAO)
                || (repositorio.acordoDe(paciente) != null && repositorio.acordoDe(paciente).isInadimplido());
    }

    public boolean agendamentoBloqueado(String paciente) {
        return verificarRestricao(paciente);
    }

    public ParcelaCobranca consultarParcela(String paciente) {
        return repositorio.vencidasDe(paciente).stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Sem parcelas para: " + paciente));
    }

    public Acordo firmarAcordo(String paciente, int quantidadeNovas, double valorNova) {
        return firmarAcordo(paciente, quantidadeNovas, "Acordo de pagamento", valorNova);
    }

    public Acordo firmarAcordo(String paciente, int quantidadeNovas, String justificativa) {
        List<ParcelaCobranca> originais = repositorio.vencidasDe(paciente);
        double valorTotal = originais.stream().mapToDouble(ParcelaCobranca::getValorAtualizado).sum();
        return firmarAcordo(paciente, quantidadeNovas, justificativa, valorTotal / quantidadeNovas);
    }

    public Acordo firmarAcordo(String paciente, int quantidadeNovas, String justificativa, double valorNova) {
        if (repositorio.vencidasDe(paciente).isEmpty()) {
            throw new IllegalStateException("É necessário possuir ao menos uma parcela vencida");
        }
        if (existeAcordoAtivo(paciente)) {
            throw new IllegalStateException("Já existe um acordo ativo para este paciente");
        }

        List<ParcelaCobranca> originais = new ArrayList<>(repositorio.vencidasDe(paciente));
        List<ParcelaCobranca> novas = new ArrayList<>();
        for (int i = 0; i < quantidadeNovas; i++) {
            ParcelaCobranca nova = new ParcelaCobranca(valorNova, 0, 0, 0);
            nova.marcarPendente();
            novas.add(nova);
        }

        Acordo acordo = new Acordo(new AcordoId(repositorio.proximoAcordoId()), paciente, originais, novas, justificativa);
        repositorio.salvarAcordo(paciente, acordo);
        DomainEventPublisher.publish(new AcordoFirmado(acordo.getId(), descreverParcelas(originais), novas.size()));
        return acordo;
    }

    public void inadimplirAcordo(String paciente) {
        Acordo acordo = acordoDe(paciente);
        acordo.inadimplir();
        repositorio.salvarAcordo(paciente, acordo);
        DomainEventPublisher.publish(new AcordoInadimplido(acordo.getId()));
    }

    public void marcarParcelaDoAcordoVencida(String paciente) {
        Acordo acordo = acordoDe(paciente);
        ParcelaCobranca referencia = novaParcelaVencida(acordo.getNovasParcelas().get(0).getValor(), 5);
        acordo.vencerPrimeiraNovaParcela(referencia.getDiasAtraso(), referencia.getMulta(), referencia.getJuros());
        repositorio.salvarAcordo(paciente, acordo);
    }

    public void atualizarSituacaoFinanceira(String paciente) {
        Acordo acordo = repositorio.acordoDe(paciente);
        if (acordo != null && acordo.getStatus() == StatusAcordo.ATIVO && acordo.possuiParcelaGeradaVencida()) {
            inadimplirAcordo(paciente);
        }
    }

    public void quitarPendencias(String paciente) {
        repositorio.removerVencidas(paciente);
    }

    public void registrarContato(String paciente, String canal) {
        ContatoCobranca contato = new ContatoCobranca(
                LocalDate.now(), RESPONSAVEL_PADRAO, canal, "Contato registrado", "Observacao informada");
        repositorio.salvarContato(paciente, contato);
    }

    public boolean semCobrancaRecente(String paciente, int dias) {
        LocalDate limite = LocalDate.now().minusDays(dias);
        return repositorio.contatosDe(paciente).stream().noneMatch(c -> !c.getDataContato().isBefore(limite));
    }

    public void cancelarAcordo(String paciente, String justificativa) {
        Acordo acordo = acordoDe(paciente);
        acordo.cancelar(justificativa, RESPONSAVEL_PADRAO);
        repositorio.salvarAcordo(paciente, acordo);
    }

    public void alterarStatusAcordo(String paciente) {
        Acordo acordo = acordoDe(paciente);
        acordo.alterarStatus(StatusAcordo.INADIMPLIDO, "Justificativa informada", RESPONSAVEL_PADRAO);
        repositorio.salvarAcordo(paciente, acordo);
    }

    public Acordo acordoDe(String paciente) {
        Acordo acordo = repositorio.acordoDe(paciente);
        if (acordo == null) {
            throw new IllegalArgumentException("Sem acordo para: " + paciente);
        }
        return acordo;
    }

    public ContatoCobranca ultimoContatoDe(String paciente) {
        List<ContatoCobranca> contatos = repositorio.contatosDe(paciente);
        if (contatos.isEmpty()) {
            throw new IllegalArgumentException("Sem contato para: " + paciente);
        }
        return contatos.get(contatos.size() - 1);
    }

    public HistoricoNegociacao ultimoHistoricoDe(String paciente) {
        List<HistoricoNegociacao> historico = acordoDe(paciente).getHistorico();
        if (historico.isEmpty()) {
            throw new IllegalArgumentException("Sem historico para: " + paciente);
        }
        return historico.get(historico.size() - 1);
    }

    public String selecionarRegistroPagamento(String paciente) {
        return "pagamentos:" + consultarParcela(paciente).getValorAtualizado();
    }

    public double totalSubstituidasNoFluxoCaixa(String paciente) {
        return acordoDe(paciente).getParcelasOriginais().stream()
                .filter(ParcelaCobranca::contaNoFluxoCaixa)
                .mapToDouble(ParcelaCobranca::getValor)
                .sum();
    }

    private boolean existeAcordoAtivo(String paciente) {
        Acordo acordo = repositorio.acordoDe(paciente);
        return acordo != null && acordo.getStatus() == StatusAcordo.ATIVO;
    }

    private ParcelaCobranca novaParcelaVencida(double valor, int diasAtraso) {
        double multa = valor * PERCENTUAL_MULTA;
        double juros = valor * PERCENTUAL_JUROS_DIA * diasAtraso;
        return new ParcelaCobranca(valor, diasAtraso, multa, juros);
    }

    private String descreverParcelas(List<ParcelaCobranca> parcelas) {
        return parcelas.stream()
                .map(p -> String.format("valor=%.2f,diasAtraso=%d", p.getValor(), p.getDiasAtraso()))
                .toList()
                .toString();
    }
}
