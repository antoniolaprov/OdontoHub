package com.g4.odontohub.medicamento.domain.service;

import com.g4.odontohub.medicamento.domain.event.MedicamentoCadastrado;
import com.g4.odontohub.medicamento.domain.event.MedicamentoInativado;
import com.g4.odontohub.medicamento.domain.model.Medicamento;
import com.g4.odontohub.medicamento.domain.model.MedicamentoId;
import com.g4.odontohub.medicamento.domain.repository.MedicamentoRepository;
import com.g4.odontohub.shared.DomainEventPublisher;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MedicamentoService {

    /** Mapeia substância de alergia -> classe farmacológica para verificação cruzada (F8 ↔ Anamnese). */
    private static final Map<String, String> ALERGIA_PARA_CLASSE = Map.of(
            "PENICILINA", "Beta-lactâmicos",
            "AMOXICILINA", "Beta-lactâmicos");

    private final MedicamentoRepository repositorio;

    public MedicamentoService(MedicamentoRepository repositorio) {
        this.repositorio = repositorio;
    }

    public Medicamento cadastrar(String nomeComercial, String principioAtivo,
                                 String categoriaTerapeutica, String classeFarmacologica) {
        validar(nomeComercial, principioAtivo, classeFarmacologica);
        if (existeDuplicata(nomeComercial, principioAtivo)) {
            throw new IllegalArgumentException(
                    "Já existe um medicamento com o mesmo nome comercial e princípio ativo");
        }
        MedicamentoId id = new MedicamentoId(repositorio.proximoId());
        Medicamento medicamento = new Medicamento(id, nomeComercial, principioAtivo,
                categoriaTerapeutica, classeFarmacologica);
        repositorio.salvar(medicamento);
        DomainEventPublisher.publish(new MedicamentoCadastrado(id, nomeComercial, classeFarmacologica));
        return medicamento;
    }

    public void inativar(String nomeComercial, String justificativa) {
        Medicamento medicamento = buscarPorNome(nomeComercial);
        medicamento.inativar();
        repositorio.salvar(medicamento);
        DomainEventPublisher.publish(new MedicamentoInativado(medicamento.getId(), justificativa));
    }

    public void adicionarPosologiaPadrao(String nomeComercial, String posologia) {
        Medicamento medicamento = buscarPorNome(nomeComercial);
        medicamento.adicionarPosologiaPadrao(posologia);
        repositorio.salvar(medicamento);
    }

    /** Lista todo o catálogo (ativos e inativos) — leitura para o frontend. */
    public List<Medicamento> todos() {
        return repositorio.todos();
    }

    public List<Medicamento> listarParaSelecao() {
        return repositorio.todos().stream()
                .filter(Medicamento::estaAtivo)
                .collect(Collectors.toList());
    }

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
