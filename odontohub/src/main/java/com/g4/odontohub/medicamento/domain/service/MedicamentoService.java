package com.g4.odontohub.medicamento.domain.service;

import com.g4.odontohub.medicamento.domain.event.MedicamentoCadastrado;
import com.g4.odontohub.medicamento.domain.event.MedicamentoInativado;
import com.g4.odontohub.medicamento.domain.model.Medicamento;
import com.g4.odontohub.medicamento.domain.model.MedicamentoId;
import com.g4.odontohub.medicamento.domain.repository.MedicamentoRepository;
import com.g4.odontohub.shared.DomainEventPublisher;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MedicamentoService {

    /** Mapeia substância de alergia -> classe farmacológica para verificação cruzada (F8 ↔ Anamnese). */
    private static final Map<String, String> ALERGIA_PARA_CLASSE = Map.of(
            "PENICILINA", "Beta-lactâmicos",
            "AMOXICILINA", "Beta-lactâmicos");

    private static final List<String> CLASSES_RECONHECIDAS = List.of(
            "Beta-lactâmicos", "AINEs", "Opioides", "Corticosteroides", "Analgésicos");

    private final MedicamentoRepository repositorio;
    private final Map<String, Integer> prescricoesUltimos30Dias = new HashMap<>();
    private final Set<String> prescricoesUltimos90Dias = new HashSet<>();

    public MedicamentoService(MedicamentoRepository repositorio) {
        this.repositorio = repositorio;
    }

    public Medicamento cadastrar(String nomeComercial, String principioAtivo,
                                 String categoriaTerapeutica, String classeFarmacologica) {
        return cadastrar(nomeComercial, principioAtivo, categoriaTerapeutica, classeFarmacologica, null, null);
    }

    public Medicamento cadastrar(String nomeComercial, String principioAtivo,
                                 String categoriaTerapeutica, String classeFarmacologica,
                                 String apresentacao, String viaAdministracao) {
        validar(nomeComercial, principioAtivo, classeFarmacologica);
        if (!CLASSES_RECONHECIDAS.contains(classeFarmacologica)) {
            throw new IllegalArgumentException("Classe farmacológica não reconhecida");
        }
        if (existeDuplicata(nomeComercial, principioAtivo)) {
            throw new IllegalArgumentException(
                    "Já existe medicamento cadastrado com este nome comercial e princípio ativo");
        }
        MedicamentoId id = new MedicamentoId(repositorio.proximoId());
        Medicamento medicamento = new Medicamento(id, nomeComercial, principioAtivo,
                categoriaTerapeutica, classeFarmacologica, apresentacao, viaAdministracao);
        repositorio.salvar(medicamento);
        DomainEventPublisher.publish(new MedicamentoCadastrado(id, nomeComercial, classeFarmacologica));
        return medicamento;
    }

    public boolean inativar(String nomeComercial, String justificativa) {
        Medicamento medicamento = buscarPorNome(nomeComercial);
        boolean possuiPrescricoes = possuiPrescricoesAtivasNosUltimos30Dias(nomeComercial);
        if (possuiPrescricoes && (justificativa == null || justificativa.isBlank())) {
            throw new IllegalArgumentException("Justificativa obrigatória para inativar medicamento com prescrições recentes");
        }
        medicamento.inativar();
        repositorio.salvar(medicamento);
        DomainEventPublisher.publish(new MedicamentoInativado(medicamento.getId(), justificativa));
        return possuiPrescricoes;
    }

    public void adicionarPosologiaPadrao(String nomeComercial, String posologia) {
        Medicamento medicamento = buscarPorNome(nomeComercial);
        medicamento.adicionarPosologiaPadrao(posologia);
        repositorio.salvar(medicamento);
    }

    public void adicionarContraindicacoes(String nomeComercial, List<String> contraindicacoes) {
        Medicamento medicamento = buscarPorNome(nomeComercial);
        contraindicacoes.forEach(medicamento::adicionarContraindicacao);
        repositorio.salvar(medicamento);
    }

    public void adicionarInteracoes(String nomeComercial, List<String> interacoes) {
        Medicamento medicamento = buscarPorNome(nomeComercial);
        interacoes.forEach(medicamento::adicionarInteracao);
        repositorio.salvar(medicamento);
    }

    public void alterarCategoriaTerapeutica(String nomeComercial, String novaCategoria, String usuario) {
        Medicamento medicamento = buscarPorNome(nomeComercial);
        medicamento.alterarCategoriaTerapeutica(novaCategoria, usuario);
        repositorio.salvar(medicamento);
    }

    public void alterarClasseFarmacologica(String nomeComercial, String novaClasseFarmacologica, String usuario) {
        if (!CLASSES_RECONHECIDAS.contains(novaClasseFarmacologica)) {
            throw new IllegalArgumentException("Classe farmacológica não reconhecida");
        }
        if (possuiPrescricoesUltimos90Dias(nomeComercial)) {
            throw new IllegalArgumentException("Classe farmacológica não pode ser alterada pois há prescrições emitidas nos últimos 90 dias");
        }
        Medicamento medicamento = buscarPorNome(nomeComercial);
        medicamento.alterarClasseFarmacologica(novaClasseFarmacologica, usuario);
        repositorio.salvar(medicamento);
    }

    public void registrarPrescricoesRecentes(String nomeComercial, int dias) {
        String chave = nomeComercial.trim().toUpperCase();
        if (dias <= 30) {
            prescricoesUltimos30Dias.put(chave, prescricoesUltimos30Dias.getOrDefault(chave, 0) + 1);
        }
        if (dias <= 90) {
            prescricoesUltimos90Dias.add(chave);
        }
    }

    public void removerPrescricoesRecentes(String nomeComercial) {
        String chave = nomeComercial.trim().toUpperCase();
        prescricoesUltimos30Dias.remove(chave);
        prescricoesUltimos90Dias.remove(chave);
    }

    public List<Medicamento> listarParaSelecao() {
        return repositorio.todos().stream()
                .filter(Medicamento::estaAtivo)
                .collect(Collectors.toList());
    }

    public boolean haContraindicacaoCruzada(String nomeComercial, String alergiaPaciente) {
        Medicamento medicamento = buscarPorNome(nomeComercial);
        if (alergiaPaciente == null || alergiaPaciente.isBlank()) {
            return false;
        }
        String classeDaAlergia = ALERGIA_PARA_CLASSE.get(alergiaPaciente.trim().toUpperCase());
        return classeDaAlergia != null
                && classeDaAlergia.equalsIgnoreCase(medicamento.getClasseFarmacologica());
    }

    public ResultadoImportacao importarLote(List<LinhaImportacao> linhas) {
        ResultadoImportacao resultado = new ResultadoImportacao();
        for (LinhaImportacao linha : linhas) {
            try {
                Medicamento medicamento = cadastrar(linha.nomeComercial(), linha.principioAtivo(),
                        linha.categoriaTerapeutica(), linha.classeFarmacologica(),
                        linha.apresentacao(), linha.viaAdministracao());
                if (linha.status() != null && linha.status().equalsIgnoreCase("Inativo")) {
                    inativar(medicamento.getNomeComercial(), "Importado inativo");
                }
                resultado.registrarSucesso(linha.nomeComercial());
            } catch (IllegalArgumentException e) {
                resultado.registrarRejeicao(linha.nomeComercial(), e.getMessage());
            }
        }
        return resultado;
    }

    public Medicamento buscarPorNome(String nomeComercial) {
        Medicamento medicamento = repositorio.buscarPorNome(nomeComercial);
        if (medicamento == null) {
            throw new IllegalArgumentException("Medicamento não encontrado: " + nomeComercial);
        }
        return medicamento;
    }

    public boolean existeParaSelecao(String nomeComercial) {
        return listarParaSelecao().stream().anyMatch(m -> m.getNomeComercial().equals(nomeComercial));
    }

    public boolean existeNoCatalogo(String nomeComercial) {
        return repositorio.buscarPorNome(nomeComercial) != null;
    }

    private boolean existeDuplicata(String nomeComercial, String principioAtivo) {
        return repositorio.todos().stream()
                .anyMatch(m -> m.getNomeComercial().equalsIgnoreCase(nomeComercial)
                        && m.getPrincipioAtivo().equalsIgnoreCase(principioAtivo));
    }

    private boolean possuiPrescricoesAtivasNosUltimos30Dias(String nomeComercial) {
        return prescricoesUltimos30Dias.containsKey(nomeComercial.trim().toUpperCase());
    }

    private boolean possuiPrescricoesUltimos90Dias(String nomeComercial) {
        return prescricoesUltimos90Dias.contains(nomeComercial.trim().toUpperCase());
    }

    private void validar(String nomeComercial, String principioAtivo, String classeFarmacologica) {
        if (nomeComercial == null || nomeComercial.isBlank()) {
            throw new IllegalArgumentException("O nome comercial é obrigatório");
        }
        if (principioAtivo == null || principioAtivo.isBlank()) {
            throw new IllegalArgumentException("Princípio ativo é obrigatório");
        }
        if (classeFarmacologica == null || classeFarmacologica.isBlank()) {
            throw new IllegalArgumentException("Classe farmacológica é obrigatória");
        }
    }

    public record LinhaImportacao(String nomeComercial, String principioAtivo,
                                  String categoriaTerapeutica, String classeFarmacologica,
                                  String apresentacao, String viaAdministracao, String status) {}
}
