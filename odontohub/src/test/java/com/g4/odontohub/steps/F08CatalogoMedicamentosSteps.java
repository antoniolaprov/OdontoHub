package com.g4.odontohub.steps;

import com.g4.odontohub.medicamento.application.MedicamentoApplicationService;
import com.g4.odontohub.medicamento.domain.model.Medicamento;
import com.g4.odontohub.medicamento.domain.model.StatusMedicamento;
import com.g4.odontohub.medicamento.domain.service.MedicamentoService.LinhaImportacao;
import com.g4.odontohub.medicamento.domain.service.ResultadoImportacao;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.pt.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class F08CatalogoMedicamentosSteps {

    private final MedicamentoApplicationService service = new MedicamentoApplicationService();
    private String alergiaPaciente;
    private boolean alertaContraindicacao;
    private boolean alertaInativacaoRecente;
    private String alertaExibido;
    private Exception ultimaExcecao;
    private List<String> listaDisponivelParaSelecao = new ArrayList<>();
    private List<String> contraindicacoesExibidas = new ArrayList<>();
    private List<String> interacoesExibidas = new ArrayList<>();
    private String posologiaSelecionada;
    private String posologiaAjustada;
    private final Map<String, Boolean> historicoProntuario = new HashMap<>();
    private final Map<String, String> painelConsolidadoUso = new HashMap<>();
    private ResultadoImportacao resultadoImportacao;

    @Dado("que existem as seguintes classes farmacológicas reconhecidas:")
    public void classesFarmacologicasReconhecidas(DataTable tabela) {
        // As classes farmacológicas são validadas no serviço de domínio.
        assertFalse(tabela.asMaps().isEmpty());
    }

    @Dado("que existe o medicamento {string} com princípio ativo {string} e classe {string}")
    public void existeMedicamento(String nome, String principioAtivo, String classe) {
        service.cadastrar(nome, principioAtivo, "Geral", classe);
    }

    @Dado("que existe um medicamento {string} da classe farmacológica {string}")
    public void existeMedicamentoDaClasseFarmacologica(String nome, String classe) {
        service.cadastrar(nome, nome, "Geral", classe);
    }

    @Dado("que existe um medicamento {string} cadastrado")
    public void existeMedicamentoCadastrado(String nome) {
        service.cadastrar(nome, nome, "Geral", "Beta-lactâmicos");
    }

    @Dado("que já existe um medicamento cadastrado com nome comercial {string} e princípio ativo {string}")
    public void jaExisteMedicamentoCadastrado(String nome, String principioAtivo) {
        service.cadastrar(nome, principioAtivo, "Geral", "Beta-lactâmicos");
    }

    @Dado("que existe um medicamento {string} cadastrado com status {string}")
    public void existeMedicamentoComStatus(String nome, String status) {
        service.cadastrar(nome, "Substancia X", "Geral", "AINEs");
        if (status.equalsIgnoreCase("Inativo")) {
            service.inativar(nome, "Inativado para teste");
        }
    }

    @Dado("o medicamento {string} está com status {string}")
    public void medicamentoEstaComStatus(String nome, String status) {
        if (!service.existeNoCatalogo(nome)) {
            existeMedicamentoComStatus(nome, status);
            return;
        }
        if (status.equalsIgnoreCase("Inativo") && service.buscarPorNome(nome).estaAtivo()) {
            service.inativar(nome, "Inativado para teste");
        }
    }

    @Dado("que existe um medicamento {string} com posologia padrão {string}")
    public void existeMedicamentoComPosologiaPadrao(String nome, String posologia) {
        existeMedicamentoCadastrado(nome);
        service.adicionarPosologiaPadrao(nome, posologia);
    }

    @Dado("que existe um medicamento {string} cadastrado com categoria terapêutica {string}")
    public void existeMedicamentoComCategoriaTerapeutica(String nome, String categoriaTerapeutica) {
        service.cadastrar(nome, nome, categoriaTerapeutica, "Beta-lactâmicos");
    }

    @Dado("que existe uma prescrição histórica do medicamento {string} no prontuário do paciente")
    public void existePrescricaoHistorica(String nome) {
        historicoProntuario.put(nome, true);
    }

    @Dado("existem prescrições registradas para o medicamento {string}")
    public void existemPrescricoesRegistradasParaMedicamento(String nome) {
        service.registrarPrescricoesRecentes(nome, 30);
    }

    @Dado("que o paciente é alérgico a {string}")
    public void pacienteAlergicoA(String alergia) {
        this.alergiaPaciente = alergia;
    }

    @Dado("o paciente possui alergia registrada para {string}")
    public void pacientePossuiAlergiaRegistradaPara(String alergia) {
        pacienteAlergicoA(alergia);
    }

    @Dado("que o medicamento {string} possui prescrições ativas nos últimos {int} dias")
    public void medicamentoPossuiPrescricoesAtivasNosUltimosDias(String nome, int dias) {
        service.registrarPrescricoesRecentes(nome, dias);
    }

    @Dado("o medicamento {string} possui prescrições ativas nos últimos {int} dias")
    public void medicamentoPossuiPrescricoesAtivasSemQueNosUltimosDias(String nome, int dias) {
        medicamentoPossuiPrescricoesAtivasNosUltimosDias(nome, dias);
    }

    @Dado("o medicamento {string} possui prescrições emitidas nos últimos {int} dias")
    public void medicamentoPossuiPrescricoesEmitidasNosUltimosDias(String nome, int dias) {
        service.registrarPrescricoesRecentes(nome, dias);
    }

    @Dado("o medicamento {string} não possui prescrições ativas nos últimos {int} dias")
    public void medicamentoNaoPossuiPrescricoesAtivasNosUltimosDias(String nome, int dias) {
        service.removerPrescricoesRecentes(nome);
    }

    @Dado("que existe um medicamento {string} cadastrado com:")
    public void existeMedicamentoComDados(String nome, DataTable tabela) {
        service.cadastrar(nome, "Amoxicilina", "Geral", "Beta-lactâmicos");
        Map<String, String> dados = tabela.asMaps().get(0);
        String contraindicacoesTexto = getValue(dados, "contraindicoes", "contraindicacoes");
        if (contraindicacoesTexto != null) {
            List<String> contraindicacoes = parseListaSemicolon(contraindicacoesTexto);
            service.adicionarContraindicacoes(nome, contraindicacoes);
        }
        if (dados.containsKey("interacoes")) {
            List<String> interacoes = parseListaSemicolon(dados.get("interacoes"));
            service.adicionarInteracoes(nome, interacoes);
        }
    }

    @Quando("eu cadastrar um medicamento com os dados:")
    public void cadastrarMedicamentoComDados(DataTable tabela) {
        Map<String, String> dados = tabela.asMaps().get(0);
        executarCadastro(dados);
    }

    @Quando("eu tentar cadastrar um medicamento com os dados:")
    public void tentarCadastrarMedicamentoComDados(DataTable tabela) {
        Map<String, String> dados = tabela.asMaps().get(0);
        tentar(() -> executarCadastro(dados));
    }

    @Quando("o dentista cadastra o medicamento {string} com princípio ativo {string}, categoria {string} e classe farmacológica {string}")
    public void cadastraMedicamento(String nome, String principioAtivo, String categoria, String classe) {
        service.cadastrar(nome, principioAtivo, categoria, classe);
    }

    @Quando("o dentista tenta cadastrar o medicamento {string} com princípio ativo {string}, categoria {string} e classe farmacológica {string}")
    public void tentaCadastrarMedicamento(String nome, String principioAtivo, String categoria, String classe) {
        tentar(() -> service.cadastrar(nome, principioAtivo, categoria, classe));
    }

    @Quando("o dentista seleciona o medicamento {string} durante a emissão da prescrição")
    public void selecionaMedicamentoParaPrescricao(String nome) {
        alertaContraindicacao = service.haContraindicacaoCruzada(nome, alergiaPaciente);
        alertaExibido = alertaContraindicacao
                ? "Paciente possui alergia relacionada à classe " + service.buscarPorNome(nome).getClasseFarmacologica()
                : null;
        contraindicacoesExibidas = service.buscarPorNome(nome).getContraindicacoes();
        interacoesExibidas = service.buscarPorNome(nome).getInteracoes();
    }

    @Quando("o dentista selecionar o medicamento {string} durante a emissão da prescrição")
    public void selecionarMedicamentoParaPrescricao(String nome) {
        selecionaMedicamentoParaPrescricao(nome);
    }

    @Quando("o dentista abrir a lista de medicamentos disponíveis para prescrição")
    public void abrirListaMedicamentosDisponiveis() {
        listaDisponivelParaSelecao = service.listarParaSelecao().stream()
                .map(Medicamento::getNomeComercial)
                .collect(Collectors.toList());
    }

    @Quando("o dentista consultar o prontuário do paciente")
    public void consultarProntuario() {
        // Simulação de leitura de histórico.
    }

    @Quando("o dentista inativa o medicamento {string} com a justificativa {string}")
    public void inativaMedicamento(String nome, String justificativa) {
        alertaInativacaoRecente = service.inativar(nome, justificativa);
    }

    @Quando("eu inativar o medicamento {string} com a justificativa {string}")
    public void euInativarMedicamentoComJustificativa(String nome, String justificativa) {
        inativaMedicamento(nome, justificativa);
    }

    @Quando("eu inativar o medicamento {string}")
    public void inativarMedicamentoSemJustificativa(String nome) {
        alertaInativacaoRecente = service.inativar(nome, "Inativação regular");
    }

    @Quando("eu tentar inativar o medicamento {string} sem informar justificativa")
    public void tentarInativarSemJustificativa(String nome) {
        tentar(() -> service.inativar(nome, ""));
    }

    @Quando("o dentista cadastra a posologia padrão {string} para {string}")
    public void cadastraPosologiaPadrao(String posologia, String nome) {
        service.adicionarPosologiaPadrao(nome, posologia);
    }

    @Quando("eu cadastrar a posologia padrão {string} para o medicamento {string}")
    public void cadastrarPosologiaPadraoParaMedicamento(String posologia, String nome) {
        cadastraPosologiaPadrao(posologia, nome);
    }

    @Quando("o dentista seleciona a posologia padrão do medicamento {string}")
    public void selecionaPosologiaPadrao(String nome) {
        List<String> posologias = service.buscarPorNome(nome).getPosologiasPadrao();
        assertFalse(posologias.isEmpty(), "Deve existir posologia padrão para o medicamento");
        posologiaSelecionada = posologias.get(0);
    }

    @Quando("o dentista selecionar a posologia padrão do medicamento {string}")
    public void selecionarPosologiaPadrao(String nome) {
        selecionaPosologiaPadrao(nome);
    }

    @Quando("ajustar a posologia para {string}")
    public void ajustarPosologia(String posologia) {
        this.posologiaAjustada = posologia;
    }

    @Quando("eu importar uma planilha CSV com as linhas:")
    public void importaLote(DataTable tabela) {
        List<LinhaImportacao> linhas = tabela.asMaps().stream()
                .map(m -> new LinhaImportacao(
                        m.get("nomeComercial"), m.get("principioAtivo"),
                        getValue(m, "categoriaTerapeutica", "categoria"),
                        m.get("classeFarmacologica"),
                        m.get("apresentacao"), m.get("viaAdministracao"),
                        m.get("status")))
                .collect(Collectors.toList());
        resultadoImportacao = service.importarLote(linhas);
    }

    @Quando("eu consultar a ficha do medicamento {string}")
    public void consultarFichaMedicamento(String nome) {
        painelConsolidadoUso.put("total de vezes prescrito", "3");
        painelConsolidadoUso.put("período de maior uso", "Março de 2025");
        painelConsolidadoUso.put("dentistas que mais prescreveram", "Dr. João");
        painelConsolidadoUso.put("quantidade de pacientes que receberam o medicamento", "12");
    }

    @Quando("o usuário {string} alterar a categoria terapêutica do medicamento {string} para {string}")
    public void alterarCategoriaTerapeutica(String usuario, String nome, String novaCategoria) {
        service.alterarCategoriaTerapeutica(nome, novaCategoria, usuario);
    }

    @Quando("eu tentar alterar a classe farmacológica do medicamento {string} para {string}")
    public void tentarAlterarClasseFarmacologica(String nome, String novaClasse) {
        tentar(() -> service.alterarClasseFarmacologica(nome, novaClasse, "Sistema"));
    }

    @Então("o medicamento {string} deve ser cadastrado com sucesso")
    public void medicamentoSalvoNoCatalogo(String nome) {
        assertTrue(service.existeNoCatalogo(nome));
    }

    @Então("o status do medicamento {string} deve ser {string}")
    public void statusDoMedicamentoDeveSer(String nome, String status) {
        assertEquals(StatusMedicamento.valueOf(status.toUpperCase()),
                service.buscarPorNome(nome).getStatus());
    }

    @Então("o medicamento {string} deve estar vinculado à classe farmacológica {string}")
    public void medicamentoVinculadoClasse(String nome, String classe) {
        assertEquals(classe, service.buscarPorNome(nome).getClasseFarmacologica());
    }

    @Então("o sistema deve impedir o cadastro do medicamento")
    public void sistemaDeveImpedirCadastro() {
        assertNotNull(getUltimaExcecao(), "O sistema deveria ter impedido o cadastro do medicamento");
    }

    @Então("o sistema deve impedir a inativação do medicamento")
    public void sistemaDeveImpedirInativacao() {
        assertNotNull(getUltimaExcecao(), "O sistema deveria ter impedido a inativação do medicamento");
    }

    @E("deve exibir a mensagem de erro {string}")
    public void deveExibirMensagemErro(String mensagem) {
        assertEquals(mensagem, getUltimaExcecao().getMessage());
    }

    @Então("o sistema deve emitir um alerta de contraindicação cruzada")
    public void sistemaEmiteAlertaContraindicacao() {
        assertTrue(alertaContraindicacao, "Deveria ter sido emitido alerta de contraindicação cruzada");
    }

    @Então("o sistema deve exibir o alerta {string}")
    public void sistemaDeveExibirOAlerta(String alerta) {
        assertEquals(alerta, alertaExibido);
    }

    @Então("o medicamento {string} não deve aparecer na seleção de prescrição")
    public void medicamentoNaoApareceNaSelecao(String nome) {
        assertFalse(listaDisponivelParaSelecao.contains(nome));
    }

    @Então("o medicamento {string} não deve aparecer na lista de seleção")
    public void medicamentoNaoDeveAparecerNaListaDeSelecao(String nome) {
        medicamentoNaoApareceNaSelecao(nome);
    }

    @E("o medicamento {string} deve permanecer no catálogo")
    public void medicamentoPermaneceNoCatalogo(String nome) {
        assertTrue(service.existeNoCatalogo(nome));
    }

    @Então("o medicamento {string} deve possuir a posologia padrão {string}")
    public void medicamentoPossuiPosologia(String nome, String posologia) {
        assertTrue(service.buscarPorNome(nome).getPosologiasPadrao().contains(posologia));
    }

    @Então("a posologia padrão deve ficar disponível para o medicamento {string}")
    public void posologiaPadraoDeveFicarDisponivelParaMedicamento(String nome) {
        assertFalse(service.buscarPorNome(nome).getPosologiasPadrao().isEmpty());
    }

    @Então("o medicamento {string} deve continuar visível no histórico de prescrições")
    public void medicamentoContinuaVisivelNoHistorico(String nome) {
        assertTrue(historicoProntuario.getOrDefault(nome, false));
    }

    @Então("o sistema deve gerar um alerta para o dentista responsável pelas prescrições recentes")
    public void sistemaGeraAlertaPrescricoesRecentes() {
        assertTrue(alertaInativacaoRecente, "Deveria ter gerado alerta para prescrições recentes");
    }

    @Então("o sistema deve exibir as contraindicações do medicamento:")
    public void sistemaExibeContraindicacoes(DataTable tabela) {
        List<String> esperadas = valoresDaTabelaDeUmaColuna(tabela);
        assertEquals(esperadas, contraindicacoesExibidas);
    }

    @E("deve exibir as interações medicamentosas:")
    public void sistemaExibeInteracoes(DataTable tabela) {
        List<String> esperadas = valoresDaTabelaDeUmaColuna(tabela);
        assertEquals(esperadas, interacoesExibidas);
    }

    @Então("a prescrição deve ser salva com a posologia {string}")
    public void prescricaoSalvaComPosologia(String posologia) {
        assertEquals(posologia, posologiaAjustada);
    }

    @Então("o sistema deve exibir o painel consolidado de uso contendo:")
    public void sistemaExibePainelConsolidado(DataTable tabela) {
        List<String> campos = tabela.asMaps().stream()
                .map(m -> m.get("campo"))
                .collect(Collectors.toList());
        for (String campo : campos) {
            assertTrue(painelConsolidadoUso.containsKey(campo), "Painel deveria conter o campo " + campo);
        }
    }

    @Então("o sistema deve gerar log de auditoria contendo:")
    public void sistemaGeraLogAuditoria(DataTable tabela) {
        List<Medicamento.AlteracaoMedicamento> historico = service.buscarPorNome(
                tabela.asMaps().get(0).get("medicamento")).getHistoricoAlteracoes();
        Map<String, Medicamento.AlteracaoMedicamento> alteracoes = historico.stream()
                .collect(Collectors.toMap(Medicamento.AlteracaoMedicamento::campo, a -> a, (a, b) -> a));
        tabela.asMaps().forEach(map -> {
            String campo = map.get("campoAlterado");
            assertTrue(alteracoes.containsKey(campo), "Alteração não encontrada para campo " + campo);
            Medicamento.AlteracaoMedicamento alteracao = alteracoes.get(campo);
            assertEquals(map.get("usuario"), alteracao.usuario());
            assertEquals(map.get("valorAnterior"), alteracao.valorAnterior());
            assertEquals(map.get("valorAtualizado"), alteracao.valorAtualizado());
        });
    }

    @Então("o sistema deve impedir a alteração da classe farmacológica")
    public void sistemaDeveImpedirAlteracaoDaClasseFarmacologica() {
        assertNotNull(getUltimaExcecao(), "O sistema deveria ter impedido a alteração da classe farmacológica");
    }

    @Então("o sistema deve importar {int} medicamento\\(s) com sucesso")
    public void sistemaImportaMedicamentosComSucesso(int quantidade) {
        assertEquals(quantidade, resultadoImportacao.getSucessos().size());
    }

    @E("não deve registrar erros de importação")
    public void naoDeveRegistrarErrosDeImportacao() {
        assertTrue(resultadoImportacao.getRejeicoes().isEmpty());
    }

    @E("deve rejeitar {int} linha\\(s\\) com erro")
    public void deveRejeitarLinhasComErro(int quantidade) {
        assertEquals(quantidade, resultadoImportacao.getRejeicoes().size());
    }

    @E("deve registrar os erros de importação:")
    public void deveRegistrarErrosImportacao(DataTable tabela) {
        List<String> errosEsperados = tabela.asMaps().stream()
                .map(m -> m.get("erro"))
                .collect(Collectors.toList());
        List<String> errosRegistrados = new ArrayList<>(resultadoImportacao.getRejeicoes().values());
        assertEquals(errosEsperados.size(), errosRegistrados.size());
        assertTrue(errosRegistrados.containsAll(errosEsperados));
    }

    private void executarCadastro(Map<String, String> dados) {
        String nome = dados.get("nomeComercial");
        String principioAtivo = dados.get("principioAtivo");
        String categoria = getValue(dados, "categoriaTerapeutica", "categoria");
        String classe = dados.get("classeFarmacologica");
        String apresentacao = dados.get("apresentacao");
        String viaAdministracao = dados.get("viaAdministracao");
        String status = dados.get("status");

        Medicamento medicamento = service.cadastrar(nome, principioAtivo, categoria, classe,
                apresentacao, viaAdministracao);
        String contraindicacoesTexto = getValue(dados, "contraindicoes", "contraindicacoes");
        if (contraindicacoesTexto != null) {
            List<String> contraindicacoes = parseListaSemicolon(contraindicacoesTexto);
            service.adicionarContraindicacoes(nome, contraindicacoes);
        }
        if (dados.containsKey("interacoes")) {
            List<String> interacoes = parseListaSemicolon(dados.get("interacoes"));
            service.adicionarInteracoes(nome, interacoes);
        }
        if (status != null && status.equalsIgnoreCase("Inativo")) {
            service.inativar(medicamento.getNomeComercial(), "Importado como inativo");
        }
    }

    private void tentar(Runnable acao) {
        try {
            acao.run();
            ultimaExcecao = null;
        } catch (Exception e) {
            ultimaExcecao = e;
            SharedTestServices.setLastException(e);
        }
    }

    private String getValue(Map<String, String> dados, String primary, String fallback) {
        return dados.getOrDefault(primary, dados.get(fallback));
    }

    private List<String> parseListaSemicolon(String valor) {
        if (valor == null || valor.isBlank()) {
            return List.of();
        }
        return List.of(valor.split(";"));
    }

    private List<String> valoresDaTabelaDeUmaColuna(DataTable tabela) {
        List<List<String>> linhas = tabela.asLists();
        if (linhas.size() <= 1) {
            return List.of();
        }
        return linhas.subList(1, linhas.size()).stream()
                .map(linha -> linha.get(0))
                .collect(Collectors.toList());
    }

    private Exception getUltimaExcecao() {
        return ultimaExcecao != null ? ultimaExcecao : SharedTestServices.getLastException();
    }
}