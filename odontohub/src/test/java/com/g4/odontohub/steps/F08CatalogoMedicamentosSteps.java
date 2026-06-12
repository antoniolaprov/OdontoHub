package com.g4.odontohub.steps;

import com.g4.odontohub.medicamento.application.MedicamentoApplicationService;
import com.g4.odontohub.medicamento.domain.model.StatusMedicamento;
import com.g4.odontohub.medicamento.domain.service.MedicamentoService.LinhaImportacao;
import com.g4.odontohub.medicamento.domain.service.ResultadoImportacao;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.pt.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class F08CatalogoMedicamentosSteps {

    private final MedicamentoApplicationService service = new MedicamentoApplicationService();
    private String alergiaPaciente;
    private boolean alertaContraindicacao;
    private ResultadoImportacao resultadoImportacao;

    @Dado("que existe o medicamento {string} com princípio ativo {string} e classe {string}")
    public void existeMedicamento(String nome, String principioAtivo, String classe) {
        service.cadastrar(nome, principioAtivo, "Geral", classe);
    }

    @Dado("que o paciente é alérgico a {string}")
    public void pacienteAlergicoA(String alergia) {
        this.alergiaPaciente = alergia;
    }

    @Quando("o dentista cadastra o medicamento {string} com princípio ativo {string}, categoria {string} e classe farmacológica {string}")
    public void cadastraMedicamento(String nome, String principioAtivo, String categoria, String classe) {
        service.cadastrar(nome, principioAtivo, categoria, classe);
    }

    @Quando("o dentista tenta cadastrar o medicamento {string} com princípio ativo {string}, categoria {string} e classe farmacológica {string}")
    public void tentaCadastrarMedicamento(String nome, String principioAtivo, String categoria, String classe) {
        tentar(() -> service.cadastrar(nome, principioAtivo, categoria, classe));
    }

    @Quando("o dentista tenta cadastrar o medicamento {string} sem princípio ativo")
    public void tentaCadastrarSemPrincipioAtivo(String nome) {
        tentar(() -> service.cadastrar(nome, "", "Analgésico", "Analgésicos"));
    }

    @Quando("o dentista seleciona o medicamento {string} para prescrição")
    public void selecionaMedicamentoParaPrescricao(String nome) {
        alertaContraindicacao = service.haContraindicacaoCruzada(nome, alergiaPaciente);
    }

    @Quando("o dentista inativa o medicamento {string} com a justificativa {string}")
    public void inativaMedicamento(String nome, String justificativa) {
        service.inativar(nome, justificativa);
    }

    @Quando("o dentista cadastra a posologia padrão {string} para {string}")
    public void cadastraPosologiaPadrao(String posologia, String nome) {
        service.adicionarPosologiaPadrao(nome, posologia);
    }

    @Quando("o dentista importa o lote de medicamentos:")
    public void importaLote(DataTable tabela) {
        List<LinhaImportacao> linhas = tabela.asMaps().stream()
                .map(m -> new LinhaImportacao(
                        m.get("nomeComercial"), m.get("principioAtivo"),
                        m.get("categoria"), m.get("classeFarmacologica")))
                .collect(Collectors.toList());
        resultadoImportacao = service.importarLote(linhas);
    }

    @Então("o medicamento {string} deve ser salvo no catálogo")
    public void medicamentoSalvoNoCatalogo(String nome) {
        assertTrue(service.existeNoCatalogo(nome));
    }

    @Então("o status do medicamento deve ser {string}")
    public void statusDoMedicamentoDeveSer(String status) {
        assertEquals(StatusMedicamento.valueOf(status.toUpperCase()),
                service.buscarPorNome("Amoxil").getStatus());
    }

    @Então("o sistema deve emitir um alerta de contraindicação cruzada")
    public void sistemaEmiteAlertaContraindicacao() {
        assertTrue(alertaContraindicacao, "Deveria ter sido emitido alerta de contraindicação cruzada");
    }

    @Então("o medicamento {string} não deve aparecer na seleção de prescrição")
    public void medicamentoNaoApareceNaSelecao(String nome) {
        assertFalse(service.existeParaSelecao(nome));
    }

    @E("o medicamento {string} deve permanecer no catálogo")
    public void medicamentoPermaneceNoCatalogo(String nome) {
        assertTrue(service.existeNoCatalogo(nome));
    }

    @Então("o medicamento {string} deve possuir a posologia padrão {string}")
    public void medicamentoPossuiPosologia(String nome, String posologia) {
        assertTrue(service.buscarPorNome(nome).getPosologiasPadrao().contains(posologia));
    }

    @Então("o medicamento {string} deve ser importado com sucesso")
    public void medicamentoImportadoComSucesso(String nome) {
        assertTrue(resultadoImportacao.foiImportado(nome));
    }

    @E("a linha {string} deve ser rejeitada com o motivo {string}")
    public void linhaRejeitadaComMotivo(String nome, String motivo) {
        assertTrue(resultadoImportacao.foiRejeitado(nome));
        assertEquals(motivo, resultadoImportacao.motivoRejeicao(nome));
    }

    private void tentar(Runnable acao) {
        try {
            acao.run();
        } catch (Exception e) {
            SharedTestServices.setLastException(e);
        }
    }
}
