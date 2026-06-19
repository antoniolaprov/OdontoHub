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

    public Medicamento cadastrar(String nomeComercial, String principioAtivo,
                                 String categoriaTerapeutica, String classeFarmacologica,
                                 String apresentacao, String viaAdministracao) {
        return service.cadastrar(nomeComercial, principioAtivo, categoriaTerapeutica,
                classeFarmacologica, apresentacao, viaAdministracao);
    }

    public boolean inativar(String nomeComercial, String justificativa) {
        return service.inativar(nomeComercial, justificativa);
    }

    public void adicionarPosologiaPadrao(String nomeComercial, String posologia) {
        service.adicionarPosologiaPadrao(nomeComercial, posologia);
    }

    public void adicionarContraindicacoes(String nomeComercial, List<String> contraindicacoes) {
        service.adicionarContraindicacoes(nomeComercial, contraindicacoes);
    }

    public void adicionarInteracoes(String nomeComercial, List<String> interacoes) {
        service.adicionarInteracoes(nomeComercial, interacoes);
    }

    public void alterarCategoriaTerapeutica(String nomeComercial, String novaCategoria, String usuario) {
        service.alterarCategoriaTerapeutica(nomeComercial, novaCategoria, usuario);
    }

    public void alterarClasseFarmacologica(String nomeComercial, String novaClasseFarmacologica, String usuario) {
        service.alterarClasseFarmacologica(nomeComercial, novaClasseFarmacologica, usuario);
    }

    public void registrarPrescricoesRecentes(String nomeComercial, int dias) {
        service.registrarPrescricoesRecentes(nomeComercial, dias);
    }

    public void removerPrescricoesRecentes(String nomeComercial) {
        service.removerPrescricoesRecentes(nomeComercial);
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
