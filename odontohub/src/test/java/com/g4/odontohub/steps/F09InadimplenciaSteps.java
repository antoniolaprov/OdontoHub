package com.g4.odontohub.steps;

import com.g4.odontohub.financeiro.application.InadimplenciaApplicationService;
import com.g4.odontohub.financeiro.domain.model.Acordo;
import com.g4.odontohub.financeiro.domain.model.ContatoCobranca;
import com.g4.odontohub.financeiro.domain.model.HistoricoNegociacao;
import com.g4.odontohub.financeiro.domain.model.ParcelaCobranca;
import com.g4.odontohub.financeiro.domain.model.StatusAcordo;
import com.g4.odontohub.financeiro.domain.model.StatusParcela;
import io.cucumber.java.pt.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * F09 — Gestão Ativa de Inadimplência e Acordos.
 *
 * <p>Liga os cenários ao {@link InadimplenciaApplicationService} do contexto
 * Financeiro (juros/multa, restrição, acordos, contatos e histórico). Exceções
 * de bloqueio são publicadas em {@link SharedTestServices} para que o step
 * global "a mensagem deve informar ..." (F11) consiga lê-las.</p>
 */
public class F09InadimplenciaSteps {

    private final InadimplenciaApplicationService service = new InadimplenciaApplicationService();

    private String paciente;
    private int diasSemCobranca;
    private ParcelaCobranca parcela;
    private List<ParcelaCobranca> inadimplentes;
    private Acordo acordo;
    private String encaminhamento;
    private Exception excecaoCapturada;

    private void tentar(Runnable acao) {
        try {
            acao.run();
        } catch (Exception e) {
            excecaoCapturada = e;
            SharedTestServices.setLastException(e);
        }
    }

    private static final java.util.Map<String, StatusParcela> STATUS_PARCELA = java.util.Map.of(
            "Substituída", StatusParcela.SUBSTITUIDA,
            "Pendente", StatusParcela.PENDENTE,
            "Vencida", StatusParcela.VENCIDA,
            "Cancelada", StatusParcela.CANCELADA,
            "Liquidada", StatusParcela.LIQUIDADA);

    // ─── Contexto ────────────────────────────────────────────────────────────

    @Dado("que o paciente {string} possui parcelas registradas no sistema")
    public void pacientePossuiParcelasRegistradas(String nome) {
        paciente = nome;
        service.registrarPaciente(nome);
    }

    // ─── Setups de parcelas / status ─────────────────────────────────────────

    @Dado("que {string} possui uma parcela vencida há {int} dias no valor de {dinheiro}")
    public void parcelaVencidaHaDiasComValor(String nome, int dias, double valor) {
        paciente = nome;
        parcela = service.registrarParcelaVencida(nome, valor, dias);
    }

    @Dado("que {string} possui uma parcela vencida há {int} dias")
    public void parcelaVencidaHaDias(String nome, int dias) {
        paciente = nome;
        parcela = service.registrarParcelaVencida(nome, 500.0, dias);
    }

    @Dado("que {string} possui uma parcela vencida")
    public void possuiUmaParcelaVencida(String nome) {
        paciente = nome;
        parcela = service.registrarParcelaVencida(nome, 500.0, 10);
    }

    @Dado("que {string} possui parcelas vencidas")
    public void possuiParcelasVencidas(String nome) {
        paciente = nome;
        service.registrarParcelaVencida(nome, 500.0, 10);
    }

    @Dado("que {string} possui duas parcelas vencidas")
    public void possuiDuasParcelasVencidas(String nome) {
        paciente = nome;
        service.registrarParcelasVencidas(nome, 2, 1000.0);
    }

    @Dado("que {string} possui parcelas vencidas com juros e multa acumulados")
    public void possuiParcelasComEncargos(String nome) {
        paciente = nome;
        service.registrarParcelaVencida(nome, 1000.0, 40);
    }

    @Dado("que {string} não possui parcelas vencidas")
    public void naoPossuiParcelasVencidas(String nome) {
        paciente = nome;
        service.registrarPaciente(nome);
    }

    @Dado("que {string} possui status {string}")
    public void possuiStatus(String nome, String status) {
        paciente = nome;
        // Restrito = parcela vencida há mais de 30 dias.
        service.registrarParcelaVencida(nome, 500.0, 31);
    }

    @Dado("não possui parcelas vencidas")
    public void eNaoPossuiParcelasVencidas() {
        service.quitarPendencias(paciente);
    }

    @Dado("não possui acordos inadimplidos")
    public void naoPossuiAcordosInadimplidos() {
        // Sem acordo inadimplido — nada a configurar.
    }

    @Dado("que {string} possui um acordo com status {string}")
    public void possuiAcordoComStatus(String nome, String status) {
        paciente = nome;
        service.registrarParcelasVencidas(nome, 2, 1000.0);
        acordo = service.firmarAcordo(nome, 2, "Acordo de pagamento");
    }

    @Dado("uma parcela do acordo encontra-se vencida")
    public void umaParcelaDoAcordoVencida() {
        service.marcarParcelaDoAcordoVencida(paciente);
    }

    @Dado("não possui tentativa de cobrança registrada nos últimos {int} dias")
    public void semTentativaDeCobranca(int dias) {
        diasSemCobranca = dias;
    }

    // ─── Ações ───────────────────────────────────────────────────────────────

    @Quando("a recepcionista consulta a lista de inadimplentes")
    public void consultaListaInadimplentes() {
        inadimplentes = service.consultarInadimplentes();
    }

    @Quando("o sistema atualizar a situação financeira do paciente")
    public void sistemaAtualizaSituacao() {
        service.atualizarSituacaoFinanceira(paciente);
    }

    @Quando("a última pendência financeira for quitada")
    public void ultimaPendenciaQuitada() {
        service.quitarPendencias(paciente);
    }

    @Quando("a recepcionista tenta registrar um novo agendamento não emergencial")
    public void tentaRegistrarAgendamento() {
        if (service.agendamentoBloqueado(paciente)) {
            tentar(() -> {
                throw new IllegalStateException("Paciente possui restrição financeira");
            });
        }
    }

    @Quando("a recepcionista registra uma tentativa de cobrança via {string}")
    public void registraTentativaCobranca(String canal) {
        service.registrarContato(paciente, canal);
    }

    @Quando("a recepcionista cria um acordo com {int} parcelas e justificativa {string}")
    public void criaAcordoComParcelas(int parcelas, String justificativa) {
        acordo = service.firmarAcordo(paciente, parcelas, justificativa);
    }

    @Quando("a recepcionista cria um acordo de pagamento")
    public void criaAcordoDePagamento() {
        acordo = service.firmarAcordo(paciente, 2, "Acordo de pagamento");
    }

    @Quando("a recepcionista tenta criar um acordo")
    public void tentaCriarAcordo() {
        tentar(() -> acordo = service.firmarAcordo(paciente, 2, "Acordo de pagamento"));
    }

    @Quando("a recepcionista tenta criar um novo acordo")
    public void tentaCriarNovoAcordo() {
        tentar(() -> service.firmarAcordo(paciente, 2, "Novo acordo"));
    }

    @Quando("a recepcionista cancela o acordo com justificativa {string}")
    public void cancelaAcordo(String justificativa) {
        service.cancelarAcordo(paciente, justificativa);
    }

    @Quando("a recepcionista altera o status do acordo")
    public void alteraStatusAcordo() {
        service.alterarStatusAcordo(paciente);
    }

    @Quando("a recepcionista seleciona a opção registrar pagamento")
    public void selecionaRegistrarPagamento() {
        encaminhamento = service.selecionarRegistroPagamento(paciente);
    }

    // ─── Asserções: identificação / valores ──────────────────────────────────

    @Então("o sistema deve exibir a parcela vencida")
    public void deveExibirParcelaVencida() {
        assertNotNull(inadimplentes);
        assertFalse(inadimplentes.isEmpty(), "Deveria haver parcela vencida na lista");
    }

    @E("deve exibir o valor original da parcela")
    public void deveExibirValorOriginal() {
        assertTrue(parcela.getValor() > 0);
    }

    @E("deve exibir os dias de atraso")
    public void deveExibirDiasDeAtraso() {
        assertTrue(parcela.getDiasAtraso() > 0);
    }

    @E("deve exibir os juros calculados automaticamente")
    public void deveExibirJuros() {
        assertTrue(parcela.getJuros() > 0, "Juros deveriam ser calculados automaticamente");
    }

    @E("deve exibir a multa calculada automaticamente")
    public void deveExibirMulta() {
        assertTrue(parcela.getMulta() > 0, "Multa deveria ser calculada automaticamente");
    }

    @E("deve exibir o valor atualizado da dívida")
    public void deveExibirValorAtualizado() {
        assertTrue(parcela.getValorAtualizado() > parcela.getValor());
    }

    // ─── Asserções: restrição / agendamento ──────────────────────────────────

    @Então("o paciente deve receber o status {string}")
    public void pacienteDeveReceberStatus(String status) {
        assertTrue(service.verificarRestricao(paciente), paciente + " deveria estar " + status);
    }

    @E("um alerta visual deve ser exibido em seu cadastro")
    public void alertaVisualNoCadastro() {
        assertTrue(service.verificarRestricao(paciente));
    }

    @Então("o status {string} deve ser removido automaticamente")
    public void statusRemovidoAutomaticamente(String status) {
        assertFalse(service.verificarRestricao(paciente), "Status " + status + " deveria ter sido removido");
    }

    @Então("o sistema deve bloquear o agendamento")
    public void sistemaBloqueiaAgendamento() {
        assertNotNull(excecaoCapturada, "O agendamento deveria ter sido bloqueado");
    }

    // ─── Asserções: tentativa de cobrança / destaque ─────────────────────────

    @Então("o sistema deve armazenar a data do contato")
    public void armazenaDataContato() {
        assertNotNull(ultimoContato().getDataContato());
    }

    @E("deve armazenar o responsável pelo contato")
    public void armazenaResponsavel() {
        assertNotNull(ultimoContato().getResponsavel());
        assertFalseBlank(ultimoContato().getResponsavel());
    }

    @E("deve armazenar o canal utilizado")
    public void armazenaCanal() {
        assertFalseBlank(ultimoContato().getCanal());
    }

    @E("deve armazenar o resultado do contato")
    public void armazenaResultado() {
        assertFalseBlank(ultimoContato().getResultado());
    }

    @E("deve armazenar a observação informada")
    public void armazenaObservacao() {
        assertFalseBlank(ultimoContato().getObservacao());
    }

    @Então("o paciente deve ser exibido com destaque visual")
    public void exibidoComDestaqueVisual() {
        assertTrue(service.semCobrancaRecente(paciente, diasSemCobranca),
                "Inadimplente sem cobrança recente deveria ter destaque visual");
    }

    // ─── Asserções: acordo ───────────────────────────────────────────────────

    @Então("as parcelas originais devem receber o status {string}")
    public void originaisRecebemStatus(String status) {
        StatusParcela esperado = STATUS_PARCELA.get(status);
        assertTrue(acordo.getParcelasOriginais().stream().allMatch(p -> p.getStatus() == esperado));
    }

    @E("um novo acordo deve ser criado com status {string}")
    public void novoAcordoComStatus(String status) {
        assertEquals(StatusAcordo.valueOf(status.toUpperCase()), acordo.getStatus());
    }

    @E("novas parcelas devem ser geradas conforme as condições negociadas")
    public void novasParcelasGeradas() {
        assertFalse(acordo.getNovasParcelas().isEmpty());
    }

    @Então("o valor total do acordo deve incluir todos os encargos calculados até a data da negociação")
    public void valorTotalIncluiEncargos() {
        double principal = acordo.getParcelasOriginais().stream()
                .mapToDouble(ParcelaCobranca::getValor).sum();
        assertTrue(acordo.getValorTotal() > principal,
                "O valor total deveria incluir juros e multa, ficando acima do principal");
    }

    @E("a recepcionista não deve poder editar juros ou multa manualmente")
    public void naoPodeEditarJurosOuMulta() {
        assertThrows(IllegalStateException.class,
                () -> acordo.getParcelasOriginais().get(0).editarJurosOuMulta(0.0));
    }

    @Então("o sistema deve bloquear a operação")
    public void sistemaBloqueiaOperacao() {
        assertNotNull(excecaoCapturada, "A operação deveria ter sido bloqueada");
    }

    @Então("o acordo deve receber o status {string}")
    public void acordoRecebeStatus(String status) {
        assertEquals(StatusAcordo.valueOf(status.toUpperCase()), service.acordoDe(paciente).getStatus());
    }

    @E("as parcelas geradas pelo acordo devem receber o status {string}")
    public void parcelasGeradasRecebemStatus(String status) {
        StatusParcela esperado = STATUS_PARCELA.get(status);
        assertTrue(service.acordoDe(paciente).getNovasParcelas().stream()
                .allMatch(p -> p.getStatus() == esperado));
    }

    @E("as parcelas originais devem retornar ao status {string}")
    public void originaisRetornamAoStatus(String status) {
        StatusParcela esperado = STATUS_PARCELA.get(status);
        assertTrue(service.acordoDe(paciente).getParcelasOriginais().stream()
                .allMatch(p -> p.getStatus() == esperado));
    }

    @E("os encargos devem ser recalculados para a data do cancelamento")
    public void encargosRecalculados() {
        assertTrue(service.acordoDe(paciente).isMultasRetroativasAtivas());
    }

    // ─── Asserções: histórico de negociação ──────────────────────────────────

    @Então("o histórico deve registrar o responsável")
    public void historicoRegistraResponsavel() {
        assertFalseBlank(ultimoHistorico().getResponsavel());
    }

    @E("deve registrar a data da alteração")
    public void historicoRegistraData() {
        assertNotNull(ultimoHistorico().getDataAlteracao());
    }

    @E("deve registrar o status anterior")
    public void historicoRegistraStatusAnterior() {
        assertNotNull(ultimoHistorico().getStatusAnterior());
    }

    @E("deve registrar o novo status")
    public void historicoRegistraNovoStatus() {
        assertNotNull(ultimoHistorico().getNovoStatus());
    }

    @E("deve registrar a justificativa informada")
    public void historicoRegistraJustificativa() {
        assertFalseBlank(ultimoHistorico().getJustificativa());
    }

    // ─── Asserções: encaminhamento p/ pagamentos ─────────────────────────────

    @Então("o sistema deve direcionar para a funcionalidade de pagamentos")
    public void direcionaParaPagamentos() {
        assertNotNull(encaminhamento);
        assertTrue(encaminhamento.startsWith("pagamentos:"));
    }

    @E("a parcela selecionada deve ser enviada automaticamente")
    public void parcelaEnviadaAutomaticamente() {
        assertFalseBlank(encaminhamento);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private ContatoCobranca ultimoContato() {
        return service.ultimoContatoDe(paciente);
    }

    private HistoricoNegociacao ultimoHistorico() {
        return service.ultimoHistoricoDe(paciente);
    }

    private static void assertFalseBlank(String valor) {
        assertNotNull(valor);
        assertFalse(valor.isBlank());
    }
}
