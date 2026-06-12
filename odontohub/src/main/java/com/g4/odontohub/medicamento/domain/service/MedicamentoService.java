package com.g4.odontohub.medicamento.domain.service;

import com.g4.odontohub.medicamento.domain.event.MedicamentoCadastrado;
import com.g4.odontohub.medicamento.domain.event.MedicamentoInativado;
import com.g4.odontohub.medicamento.domain.model.Medicamento;
import com.g4.odontohub.medicamento.domain.model.MedicamentoId;
import com.g4.odontohub.shared.DomainEventPublisher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MedicamentoService {

    private final Map<Long, Medicamento> repositorio = new HashMap<>();
    private long nextId = 1;

    /** Mapeia substância de alergia -> classe farmacológica para verificação cruzada (F8 ↔ Anamnese). */
    private static final Map<String, String> ALERGIA_PARA_CLASSE = Map.of(
            "PENICILINA", "Beta-lactâmicos",
            "AMOXICILINA", "Beta-lactâmicos");

    public Medicamento cadastrar(String nomeComercial, String principioAtivo,
                                 String categoriaTerapeutica, String classeFarmacologica) {
        validar(nomeComercial, principioAtivo, classeFarmacologica);
        if (existeDuplicata(nomeComercial, principioAtivo)) {
            throw new IllegalArgumentException(
                    "Já existe um medicamento com o mesmo nome comercial e princípio ativo");
        }
        MedicamentoId id = new MedicamentoId(nextId++);
        Medicamento medicamento = new Medicamento(id, nomeComercial, principioAtivo,
                categoriaTerapeutica, classeFarmacologica);
        repositorio.put(id.id(), medicamento);
        DomainEventPublisher.publish(new MedicamentoCadastrado(id, nomeComercial, classeFarmacologica));
        return medicamento;
    }

    public void inativar(String nomeComercial, String justificativa) {
        Medicamento medicamento = buscarPorNome(nomeComercial);
        medicamento.inativar();
        DomainEventPublisher.publish(new MedicamentoInativado(medicamento.getId(), justificativa));
    }

    public void adicionarPosologiaPadrao(String nomeComercial, String posologia) {
        buscarPorNome(nomeComercial).adicionarPosologiaPadrao(posologia);
    }

    public List<Medicamento> listarParaSelecao() {
        return repositorio.values().stream()
                .filter(Medicamento::estaAtivo)
                .collect(Collectors.toList());
    }

    /** Verifica contraindicação cruzada: alergia do paciente x classe farmacológica do medicamento. */
    public boolean haContraindicacaoCruzada(String nomeComercial, String alergiaPaciente) {
        Medicamento medicamento = buscarPorNome(nomeComercial);
        String classeDaAlergia = ALERGIA_PARA_CLASSE.get(alergiaPaciente.trim().toUpperCase());
        return classeDaAlergia != null
                && classeDaAlergia.equalsIgnoreCase(medicamento.getClasseFarmacologica());
    }

    public ResultadoImportacao importarLote(List<LinhaImportacao> linhas) {
        ResultadoImportacao resultado = new ResultadoImportacao();
        for (LinhaImportacao linha : linhas) {
            try {
                cadastrar(linha.nomeComercial(), linha.principioAtivo(),
                        linha.categoriaTerapeutica(), linha.classeFarmacologica());
                resultado.registrarSucesso(linha.nomeComercial());
            } catch (IllegalArgumentException e) {
                resultado.registrarRejeicao(linha.nomeComercial(), e.getMessage());
            }
        }
        return resultado;
    }

    public Medicamento buscarPorNome(String nomeComercial) {
        return repositorio.values().stream()
                .filter(m -> m.getNomeComercial().equals(nomeComercial))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Medicamento não encontrado: " + nomeComercial));
    }

    public boolean existeParaSelecao(String nomeComercial) {
        return listarParaSelecao().stream().anyMatch(m -> m.getNomeComercial().equals(nomeComercial));
    }

    public boolean existeNoCatalogo(String nomeComercial) {
        return repositorio.values().stream().anyMatch(m -> m.getNomeComercial().equals(nomeComercial));
    }

    private boolean existeDuplicata(String nomeComercial, String principioAtivo) {
        return repositorio.values().stream()
                .anyMatch(m -> m.getNomeComercial().equalsIgnoreCase(nomeComercial)
                        && m.getPrincipioAtivo().equalsIgnoreCase(principioAtivo));
    }

    private void validar(String nomeComercial, String principioAtivo, String classeFarmacologica) {
        if (nomeComercial == null || nomeComercial.isBlank()) {
            throw new IllegalArgumentException("Nome comercial é obrigatório");
        }
        if (principioAtivo == null || principioAtivo.isBlank()) {
            throw new IllegalArgumentException("Princípio ativo é obrigatório");
        }
        if (classeFarmacologica == null || classeFarmacologica.isBlank()) {
            throw new IllegalArgumentException("Classe farmacológica é obrigatória");
        }
    }

    public record LinhaImportacao(String nomeComercial, String principioAtivo,
                                  String categoriaTerapeutica, String classeFarmacologica) {}
}
