package com.g4.odontohub.steps;

import com.g4.odontohub.agendamento.application.AgendamentoApplicationService;
import com.g4.odontohub.agendamento.domain.model.Agendamento;
import com.g4.odontohub.agendamento.domain.model.EntradaHistorico;
import com.g4.odontohub.agendamento.domain.model.StatusAgendamento;
import com.g4.odontohub.agendamento.domain.model.TipoAtendimento;
import io.cucumber.java.pt.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

public class F01AgendamentoSteps {

    private static final DateTimeFormatter DATA_HORA_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATA_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final String PACIENTE_PADRAO = "João Silva";
    private static final String DENTISTA_PADRAO = "Dra. Sofia Martins";
    private static final String RESPONSAVEL_RECEPCAO = "Recepcionista";

    private final AgendamentoApplicationService service = new AgendamentoApplicationService();
    private Agendamento ultimoAgendamento;
    private Exception excecaoCapturada;

    @Dado("que o dentista {string} está cadastrado no sistema")
    public void queODentistaEstaCadastrado(String nome) {
        service.cadastrarDentista(nome);
    }

    @Dado("que o paciente {string} está cadastrado no sistema")
    public void queOPacienteEstaCadastrado(String nome) {
        service.cadastrarPaciente(nome);
    }

    @Dado("que o paciente {string} não possui Plano de Tratamento ativo")
    public void pacienteSemPlanoAtivo(String nome) {
        service.definirPlanoAtivo(nome, false);
    }

    @Dado("que o paciente {string} possui um Plano de Tratamento ativo")
    public void pacienteComPlanoAtivo(String nome) {
        service.definirPlanoAtivo(nome, true);
    }

    @Dado("que o paciente {string} possui um Plano de Tratamento com status {string}")
    public void pacienteComPlanoAtivoLegado(String nome, String status) {
        service.definirPlanoAtivo(nome, true);
    }

    @Dado("que já existe um agendamento com status diferente de {string} para {string} em {string}")
    public void jaExisteAgendamentoComStatusDiferenteParaDentistaEm(String status, String nomeDentista, String dataHoraStr) {
        LocalDateTime dataHora = LocalDateTime.parse(dataHoraStr, DATA_HORA_FORMATTER);
        ultimoAgendamento = service.registrarAgendamento(PACIENTE_PADRAO, nomeDentista, dataHora);
    }

    @Dado("que já existe um agendamento confirmado para {string} em {string}")
    public void jaExisteAgendamentoConfirmado(String nomeDentista, String dataHoraStr) {
        LocalDateTime dataHora = LocalDateTime.parse(dataHoraStr, DATA_HORA_FORMATTER);
        ultimoAgendamento = service.registrarAgendamento(PACIENTE_PADRAO, nomeDentista, dataHora);
        service.confirmarAgendamento(ultimoAgendamento.getId(), RESPONSAVEL_RECEPCAO);
    }

    @Dado("que o paciente {string} possui a flag {string} como verdadeira")
    public void pacientePossuiFlagComoVerdadeira(String nome, String flag) {
        if ("inadimplente".equalsIgnoreCase(flag)) {
            service.definirInadimplente(nome, true);
        }
    }

    @Dado("que o paciente {string} possui parcelas vencidas há mais de 30 dias")
    public void pacienteInadimplenteLegado(String nome) {
        service.definirInadimplente(nome, true);
    }

    @Dado("que existe um agendamento com status {string} para {string}")
    public void existeAgendamentoComStatus(String status, String nomePaciente) {
        ultimoAgendamento = service.registrarAgendamento(nomePaciente, DENTISTA_PADRAO, LocalDateTime.now().plusDays(10));
        aplicarStatus(status);
    }

    @Dado("que existe um agendamento com status {string} para {string} em {string}")
    public void existeAgendamentoComStatusEData(String status, String nomePaciente, String dataHoraStr) {
        LocalDateTime dataHora = LocalDateTime.parse(dataHoraStr, DATA_HORA_FORMATTER);
        ultimoAgendamento = service.registrarAgendamento(nomePaciente, DENTISTA_PADRAO, dataHora);
        aplicarStatus(status);
    }

    @Dado("que existe um agendamento com status diferente de {string} para {string}")
    public void existeAgendamentoComStatusDiferente(String status, String nomePaciente) {
        ultimoAgendamento = service.registrarAgendamento(nomePaciente, DENTISTA_PADRAO, LocalDateTime.now().plusDays(10));
    }

    @Dado("que existe um agendamento com status diferente de {string} para {string} em {string}")
    public void existeAgendamentoComStatusDiferenteEData(String status, String nomePaciente, String dataHoraStr) {
        LocalDateTime dataHora = LocalDateTime.parse(dataHoraStr, DATA_HORA_FORMATTER);
        ultimoAgendamento = service.registrarAgendamento(nomePaciente, DENTISTA_PADRAO, dataHora);
    }

    @Quando("a recepcionista registra um agendamento para {string} com {string} em {string}")
    public void registrarAgendamento(String nomePaciente, String nomeDentista, String dataHoraStr) {
        ultimoAgendamento = service.registrarAgendamento(
                nomePaciente, nomeDentista, LocalDateTime.parse(dataHoraStr, DATA_HORA_FORMATTER));
    }

    @Quando("a recepcionista tenta registrar outro agendamento para {string} em {string}")
    public void tentaRegistrarOutroAgendamento(String nomeDentista, String dataHoraStr) {
        tentarExecutar(() -> ultimoAgendamento = service.registrarAgendamento(
                PACIENTE_PADRAO, nomeDentista, LocalDateTime.parse(dataHoraStr, DATA_HORA_FORMATTER)));
    }

    @Quando("a recepcionista tenta registrar um agendamento para {string}")
    public void tentaRegistrarAgendamentoPara(String nomePaciente) {
        tentarExecutar(() -> ultimoAgendamento = service.registrarAgendamento(
                nomePaciente, DENTISTA_PADRAO, LocalDateTime.now().plusDays(5)));
    }

    @Quando("a recepcionista tenta registrar um agendamento para {string} com {string} em {string}")
    public void tentaRegistrarAgendamento(String nomePaciente, String nomeDentista, String dataHoraStr) {
        tentarExecutar(() -> ultimoAgendamento = service.registrarAgendamento(
                nomePaciente, nomeDentista, LocalDateTime.parse(dataHoraStr, DATA_HORA_FORMATTER)));
    }

    @Quando("a recepcionista confirma o agendamento")
    public void confirmarAgendamento() {
        service.confirmarAgendamento(ultimoAgendamento.getId(), RESPONSAVEL_RECEPCAO);
    }

    @Quando("a recepcionista {string} confirma o agendamento")
    public void confirmarAgendamentoLegado(String responsavel) {
        service.confirmarAgendamento(ultimoAgendamento.getId(), responsavel);
    }

    @Quando("a recepcionista tenta cancelar o agendamento sem informar motivo")
    public void tentaCancelarSemMotivo() {
        tentarExecutar(() -> service.cancelarAgendamento(ultimoAgendamento.getId(), "", RESPONSAVEL_RECEPCAO));
    }

    @Quando("a recepcionista cancela o agendamento informando o motivo {string}")
    public void cancelarAgendamento(String motivo) {
        service.cancelarAgendamento(ultimoAgendamento.getId(), motivo, RESPONSAVEL_RECEPCAO);
    }

    @Quando("a recepcionista remarca o agendamento para {string}")
    public void remarcarAgendamento(String novaDataHoraStr) {
        service.remarcarAgendamento(
                ultimoAgendamento.getId(), LocalDateTime.parse(novaDataHoraStr, DATA_HORA_FORMATTER), RESPONSAVEL_RECEPCAO);
    }

    @Quando("a recepcionista {string} remarca o agendamento para {string}")
    public void remarcarAgendamentoLegado(String responsavel, String novaDataHoraStr) {
        service.remarcarAgendamento(
                ultimoAgendamento.getId(), LocalDateTime.parse(novaDataHoraStr, DATA_HORA_FORMATTER), responsavel);
    }

    @Quando("a recepcionista tenta remarcar o agendamento para {string}")
    public void tentaRemarcarAgendamento(String novaDataHoraStr) {
        tentarExecutar(() -> service.remarcarAgendamento(
                ultimoAgendamento.getId(), LocalDateTime.parse(novaDataHoraStr, DATA_HORA_FORMATTER), RESPONSAVEL_RECEPCAO));
    }

    @Então("o agendamento deve ser criado com o tipo {string}")
    public void agendamentoDeveSerCriadoComTipo(String tipoEsperado) {
        assertNotNull(ultimoAgendamento);
        assertEquals(TipoAtendimento.valueOf(tipoEsperado), ultimoAgendamento.getTipo());
    }

    @Então("o status do agendamento deve ser {string}")
    public void statusDoAgendamentoDeveSer(String statusEsperado) {
        assertNotNull(ultimoAgendamento);
        assertEquals(StatusAgendamento.valueOf(statusEsperado), ultimoAgendamento.getStatus());
    }

    @Então("o sistema deve rejeitar o agendamento")
    public void sistemaDeveRejeitarOAgendamento() {
        assertNotNull(excecaoCapturada, "O sistema deveria ter rejeitado o agendamento");
    }

    @Então("o sistema deve exibir o alerta visual {string}")
    public void sistemaDeveExibirAlertaVisual(String alerta) {
        assertNotNull(excecaoCapturada, "O sistema deveria ter exibido alerta visual");
        assertEquals("Paciente inadimplente", excecaoCapturada.getMessage());
    }

    @Então("o agendamento não deve ser criado")
    public void agendamentoNaoDeveSerCriado() {
        assertTrue(ultimoAgendamento == null || excecaoCapturada != null);
    }

    @Então("o sistema deve rejeitar o cancelamento")
    public void sistemaDeveRejeitarCancelamento() {
        assertNotNull(excecaoCapturada, "O sistema deveria ter rejeitado o cancelamento");
    }

    @Então("o sistema deve rejeitar a remarcação")
    public void sistemaDeveRejeitarRemarcacao() {
        assertNotNull(excecaoCapturada, "O sistema deveria ter rejeitado a remarcação");
    }

    @Então("o histórico deve conter uma entrada com a ação {string}")
    public void historicoDeveConterAcao(String acao) {
        assertTrue(ultimoAgendamento.getHistorico().stream().anyMatch(e -> e.getAcao().equals(acao)),
                "Histórico não contém a ação: " + acao);
    }

    @Então("o responsável registrado no histórico deve ser {string}")
    public void responsavelRegistradoNoHistorico(String responsavel) {
        EntradaHistorico entrada = ultimoAgendamento.getHistorico().get(ultimoAgendamento.getHistorico().size() - 1);
        assertEquals(responsavel, entrada.getResponsavel());
    }

    @Então("a data da última alteração deve ser atualizada para hoje")
    public void dataUltimaAlteracaoAtualizadaParaHoje() {
        assertNotNull(ultimoAgendamento.getDataUltimaAlteracao());
        assertEquals(LocalDate.now(), ultimoAgendamento.getDataUltimaAlteracao().toLocalDate());
    }

    @Então("o campo {string} deve ser registrado como {string}")
    public void campoDeveSerRegistradoComo(String campo, String valor) {
        if ("motivoCancelamento".equals(campo)) {
            assertEquals(valor, ultimoAgendamento.getMotivoCancelamento());
            return;
        }
        fail("Campo não suportado: " + campo);
    }

    @Então("a nova data do agendamento deve ser {string}")
    public void novaDataDoAgendamentoDeveSer(String dataEsperada) {
        assertEquals(dataEsperada, ultimoAgendamento.getDataHora().format(DATA_FORMATTER));
    }

    @Então("o novo horário deve ser {string}")
    public void novoHorarioDeveSer(String horaEsperada) {
        assertEquals(horaEsperada, ultimoAgendamento.getDataHora().format(HORA_FORMATTER));
    }

    @E("o responsável pela alteração deve ser registrado como {string}")
    public void responsavelPelaAlteracao(String responsavel) {
        assertEquals(responsavel, ultimoAgendamento.getResponsavelAlteracao());
    }

    @E("a data da última alteração deve ser registrada")
    public void dataUltimaAlteracaoDeveSerRegistrada() {
        assertNotNull(ultimoAgendamento.getDataUltimaAlteracao());
    }

    @E("o motivo de cancelamento deve ser registrado como {string}")
    public void motivoDeCancelamento(String motivo) {
        assertEquals(motivo, ultimoAgendamento.getMotivoCancelamento());
    }

    @E("a nova data deve ser {string}")
    public void aNovaDatDeveSer(String dataHoraStr) {
        assertEquals(LocalDateTime.parse(dataHoraStr, DATA_HORA_FORMATTER), ultimoAgendamento.getDataHora());
    }

    private void aplicarStatus(String status) {
        StatusAgendamento statusAgendamento = StatusAgendamento.valueOf(status.toUpperCase());
        if (statusAgendamento == StatusAgendamento.CONFIRMADO) {
            service.confirmarAgendamento(ultimoAgendamento.getId(), RESPONSAVEL_RECEPCAO);
        } else if (statusAgendamento == StatusAgendamento.CANCELADO) {
            service.cancelarAgendamento(ultimoAgendamento.getId(), "Cancelamento de teste", RESPONSAVEL_RECEPCAO);
        } else if (statusAgendamento == StatusAgendamento.REMARCADO) {
            service.remarcarAgendamento(ultimoAgendamento.getId(), LocalDateTime.now().plusDays(11), RESPONSAVEL_RECEPCAO);
        }
    }

    private void tentarExecutar(Operacao operacao) {
        try {
            excecaoCapturada = null;
            operacao.executar();
        } catch (Exception e) {
            excecaoCapturada = e;
            SharedTestServices.setLastException(e);
        }
    }

    @FunctionalInterface
    private interface Operacao {
        void executar();
    }
}
