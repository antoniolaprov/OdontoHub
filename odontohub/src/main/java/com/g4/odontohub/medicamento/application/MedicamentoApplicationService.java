package com.g4.odontohub.medicamento.application;

import com.g4.odontohub.medicamento.domain.model.Medicamento;
import com.g4.odontohub.medicamento.domain.repository.MedicamentoRepository;
import com.g4.odontohub.medicamento.domain.service.MedicamentoService;
import com.g4.odontohub.medicamento.domain.service.MedicamentoService.LinhaImportacao;
import com.g4.odontohub.medicamento.domain.service.ResultadoImportacao;
import com.g4.odontohub.medicamento.infrastructure.persistence.InMemoryMedicamentoRepository;

import java.util.List;

public class MedicamentoApplicationService {

    private final MedicamentoService service;

    public MedicamentoApplicationService() {
        this(new InMemoryMedicamentoRepository());
    }

    public MedicamentoApplicationService(MedicamentoRepository repositorio) {
        this.service = new MedicamentoService(repositorio);
    }

    public Medicamento cadastrar(String nomeComercial, String principioAtivo,
                                 String categoriaTerapeutica, String classeFarmacologica) {
        return service.cadastrar(nomeComercial, principioAtivo, categoriaTerapeutica, classeFarmacologica);
    }

    public void inativar(String nomeComercial, String justificativa) {
        service.inativar(nomeComercial, justificativa);
    }

    public void adicionarPosologiaPadrao(String nomeComercial, String posologia) {
        service.adicionarPosologiaPadrao(nomeComercial, posologia);
    }

    public List<Medicamento> listarParaSelecao() {
        return service.listarParaSelecao();
    }

    public boolean haContraindicacaoCruzada(String nomeComercial, String alergiaPaciente) {
        return service.haContraindicacaoCruzada(nomeComercial, alergiaPaciente);
    }

    public ResultadoImportacao importarLote(List<LinhaImportacao> linhas) {
        return service.importarLote(linhas);
    }

    public Medicamento buscarPorNome(String nomeComercial) {
        return service.buscarPorNome(nomeComercial);
    }

    public boolean existeParaSelecao(String nomeComercial) {
        return service.existeParaSelecao(nomeComercial);
    }

    public boolean existeNoCatalogo(String nomeComercial) {
        return service.existeNoCatalogo(nomeComercial);
    }
}
