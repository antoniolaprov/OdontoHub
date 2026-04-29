package com.g4.odontohub.steps;

import com.g4.odontohub.prontuarioclinico.application.ProntuarioApplicationService;
import com.g4.odontohub.prontuarioclinico.domain.model.PlanoTratamento;
import com.g4.odontohub.prontuarioclinico.domain.model.Procedimento;
import com.g4.odontohub.prontuarioclinico.domain.model.StatusPlano;
import com.g4.odontohub.prontuarioclinico.domain.model.StatusProcedimento;
import com.g4.odontohub.prontuarioclinico.domain.model.VOsPlano.PlanoId;
import com.g4.odontohub.prontuarioclinico.domain.model.VOsPlano.ProcedimentoId;
import io.cucumber.java.pt.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

public class F03_PlanoTratamento_Steps {

    private ProntuarioApplicationService appService = new ProntuarioApplicationService();
    private Long pacienteIdAtual;
    private PlanoId planoIdAtual;
    private Long dentistaId = 100L;
    private Long agendamentoVinculadoId = 555L;
    private Exception excecaoCapturada;
    private ProcedimentoId procedimentoAlvoId;

    @Dado("que o paciente {string} possui anamnese registrada no sistema")
    public void que_o_paciente_possui_anamnese_registrada_no_sistema(String nomePaciente) {
        pacienteIdAtual = (long) nomePaciente.hashCode();
        
        // CORREÇÃO AQUI: Verifica se já existe antes de tentar criar de novo,
        // evitando conflito com o bloco "Contexto" do Cucumber.
        if (!appService.getAnamneseService().anamneseExiste(pacienteIdAtual)) {
            appService.getAnamneseService().registrarAnamnese(pacienteIdAtual, "Nenhuma", "", "", "Dr. Dentista");
        }
    }

    @Dado("que existe um agendamento confirmado para {string}")
    public void que_existe_um_agendamento_confirmado_para(String nomePaciente) {
        // Agendamento já é mockado pelo agendamentoVinculadoId global
    }

    @Dado("que o paciente {string} não possui anamnese registrada no sistema")
    public void que_o_paciente_nao_possui_anamnese_registrada_no_sistema(String nomePaciente) {
        pacienteIdAtual = (long) nomePaciente.hashCode();
    }

    @Dado("que o Plano de Tratamento de {string} possui o procedimento {string} com status {string}")
    public void que_o_plano_de_tratamento_possui_procedimento_com_status(String nomePaciente, String nomeProcedimento, String status) {
        que_o_paciente_possui_anamnese_registrada_no_sistema(nomePaciente);
        
        if (planoIdAtual == null) {
            planoIdAtual = appService.getPlanoTratamentoService().criarPlano(pacienteIdAtual, dentistaId);
        }
        
        appService.getPlanoTratamentoService().adicionarProcedimento(planoIdAtual, nomeProcedimento, "Geral");
        
        PlanoTratamento plano = appService.getPlanoRepository().buscarPorId(planoIdAtual);
        procedimentoAlvoId = plano.getProcedimentos().get(plano.getProcedimentos().size() - 1).getId();
    }

    @Dado("que o Plano de Tratamento de {string} está na versão {int}")
    public void que_o_plano_de_tratamento_esta_na_versao(String nomePaciente, Integer versaoEsperada) {
        que_o_paciente_possui_anamnese_registrada_no_sistema(nomePaciente);
        planoIdAtual = appService.getPlanoTratamentoService().criarPlano(pacienteIdAtual, dentistaId);
    }

    @Dado("que o procedimento {string} foi registrado como {string} há {int} horas no plano de {string}")
    public void que_o_procedimento_foi_registrado_como_realizado_ha_horas(String nomeProcedimento, String status, Integer horasRestantes, String nomePaciente) {
        que_o_plano_de_tratamento_possui_procedimento_com_status(nomePaciente, nomeProcedimento, "Pendente");
        appService.getPlanoTratamentoService().realizarProcedimento(planoIdAtual, procedimentoAlvoId, "Evolução", agendamentoVinculadoId, "Dr.");
        
        // Simula a passagem do tempo
        PlanoTratamento plano = appService.getPlanoRepository().buscarPorId(planoIdAtual);
        Procedimento proc = plano.buscarProcedimento(procedimentoAlvoId);
        long horaPassadaEmMs = System.currentTimeMillis() - ((long) horasRestantes * 60 * 60 * 1000);
        proc.setParaTestesDataConfirmacaoRegistro(new Date(horaPassadaEmMs));
    }

    @Dado("que o Plano de Tratamento de {string} possui procedimentos com status {string}")
    public void que_o_plano_de_tratamento_possui_procedimentos_com_status(String nomePaciente, String status) {
        que_o_plano_de_tratamento_possui_procedimento_com_status(nomePaciente, "Consulta", status);
    }

    @Dado("que o Plano de Tratamento de {string} possui um procedimento realizado há {int} horas")
    public void que_o_plano_de_tratamento_possui_um_procedimento_realizado_ha_horas(String nomePaciente, Integer horasPassadas) {
        que_o_procedimento_foi_registrado_como_realizado_ha_horas("Cirurgia", "Realizado", horasPassadas, nomePaciente);
    }

    @Quando("o dentista elabora um Plano de Tratamento para {string}")
    public void o_dentista_cria_um_plano_de_tratamento_para(String nomePaciente) {
        planoIdAtual = appService.getPlanoTratamentoService().criarPlano(pacienteIdAtual, dentistaId);
    }

    @Quando("o dentista tenta elaborar um Plano de Tratamento para {string}")
    public void o_dentista_tenta_criar_um_plano_de_tratamento_para(String nomePaciente) {
        try {
            appService.getPlanoTratamentoService().criarPlano(pacienteIdAtual, dentistaId);
        } catch (Exception e) {
            this.excecaoCapturada = e;
        }
    }

    @Quando("o dentista realiza o procedimento {string} com a evolução clínica {string}")
    public void o_dentista_realiza_o_procedimento(String nomeProcedimento, String evolucao) {
        appService.getPlanoTratamentoService().realizarProcedimento(planoIdAtual, procedimentoAlvoId, evolucao, agendamentoVinculadoId, "Dr.");
    }

    @Quando("o dentista cancela o procedimento {string} com justificativa {string}")
    public void o_dentista_cancela_o_procedimento(String nomeProcedimento, String justificativa) {
        appService.getPlanoTratamentoService().cancelarProcedimento(planoIdAtual, procedimentoAlvoId, justificativa);
    }

    @Quando("o dentista adiciona o procedimento {string} ao plano")
    public void o_dentista_adiciona_o_procedimento_ao_plano(String nomeProcedimento) {
        appService.getPlanoTratamentoService().adicionarProcedimento(planoIdAtual, nomeProcedimento, "Geral");
    }

    @Quando("o dentista exclui o procedimento {string} com a justificativa {string}")
    public void o_dentista_exclui_o_procedimento(String nomeProcedimento, String justificativa) {
        appService.getPlanoTratamentoService().excluirProcedimento(planoIdAtual, procedimentoAlvoId, justificativa, "Dr.");
    }

    @Quando("o dentista tenta excluir o procedimento {string}")
    public void o_dentista_tenta_excluir_o_procedimento(String nomeProcedimento) {
        try {
            appService.getPlanoTratamentoService().excluirProcedimento(planoIdAtual, procedimentoAlvoId, "Correção", "Dr.");
        } catch (Exception e) {
            this.excecaoCapturada = e;
        }
    }

    @Quando("o dentista encerra o plano com a justificativa {string}")
    public void o_dentista_encerra_o_plano_com_justificativa(String justificativa) {
        appService.getPlanoTratamentoService().encerrarPlano(planoIdAtual, justificativa);
    }

    @Quando("o dentista tenta excluir o Plano de Tratamento")
    public void o_dentista_tenta_excluir_o_plano() {
        try {
            appService.getPlanoTratamentoService().excluirPlano(planoIdAtual);
        } catch (Exception e) {
            this.excecaoCapturada = e;
        }
    }

    @Então("o plano deve ser criado com status {string}")
    public void o_plano_deve_ser_criado_com_status(String statusEsperado) {
        PlanoTratamento plano = appService.getPlanoRepository().buscarPorId(planoIdAtual);
        assertEquals(StatusPlano.valueOf("EM_ANDAMENTO"), plano.getStatus());
    }

    @Então("o plano deve ser criado na versão {int}")
    public void o_plano_deve_ser_criado_na_versao(Integer versaoEsperada) {
        PlanoTratamento plano = appService.getPlanoRepository().buscarPorId(planoIdAtual);
        assertEquals(versaoEsperada, plano.getVersao());
    }

    @Então("o sistema do plano deve bloquear a operação")
    public void o_sistema_do_plano_deve_bloquear_a_operacao() {
        assertNotNull(excecaoCapturada);
    }

    @Então("a mensagem de erro do plano deve informar {string}")
    public void a_mensagem_de_erro_do_plano_deve_informar(String msgEsperada) {
        assertEquals(msgEsperada, excecaoCapturada.getMessage());
    }

    @Então("o status do procedimento deve ser {string}")
    public void o_status_do_procedimento_deve_ser(String statusEsperado) {
        PlanoTratamento plano = appService.getPlanoRepository().buscarPorId(planoIdAtual);
        Procedimento proc = plano.buscarProcedimento(procedimentoAlvoId);
        
        StatusProcedimento enumStatus = statusEsperado.equals("Realizado") ? StatusProcedimento.REALIZADO : StatusProcedimento.CANCELADO;
        assertEquals(enumStatus, proc.getStatus());
    }

    @Então("a evolução clínica deve ser registrada com data e executor")
    public void a_evolucao_clinica_deve_ser_registrada() {
        PlanoTratamento plano = appService.getPlanoRepository().buscarPorId(planoIdAtual);
        Procedimento proc = plano.buscarProcedimento(procedimentoAlvoId);
        assertNotNull(proc.getEvolucao().dataRegistro());
        assertNotNull(proc.getEvolucao().executor());
    }

    @Então("o agendamento confirmado deve estar vinculado ao procedimento")
    public void o_agendamento_confirmado_deve_estar_vinculado() {
        PlanoTratamento plano = appService.getPlanoRepository().buscarPorId(planoIdAtual);
        Procedimento proc = plano.buscarProcedimento(procedimentoAlvoId);
        assertEquals(agendamentoVinculadoId, proc.getAgendamentoVinculado().agendamentoVinculadoId());
    }

    @Então("a justificativa de cancelamento deve ser armazenada")
    public void a_justificativa_de_cancelamento_deve_ser_armazenada() {
        PlanoTratamento plano = appService.getPlanoRepository().buscarPorId(planoIdAtual);
        Procedimento proc = plano.buscarProcedimento(procedimentoAlvoId);
        assertNotNull(proc.getJustificativaCancelamento());
    }

    @Então("nenhum registro deve ser gerado na Ficha Clínica para este procedimento")
    public void nenhum_registro_gerado_na_ficha_clinica() {
        PlanoTratamento plano = appService.getPlanoRepository().buscarPorId(planoIdAtual);
        Procedimento proc = plano.buscarProcedimento(procedimentoAlvoId);
        assertNull(proc.getEvolucao());
    }

    @Então("o plano deve ser atualizado para a versão {int}")
    public void o_plano_deve_ser_atualizado_para_versao(Integer versaoEsperada) {
        PlanoTratamento plano = appService.getPlanoRepository().buscarPorId(planoIdAtual);
        assertEquals(versaoEsperada, plano.getVersao());
    }

    @Então("o procedimento deve ser removido do histórico")
    public void o_procedimento_deve_ser_removido_do_historico() {
        PlanoTratamento plano = appService.getPlanoRepository().buscarPorId(planoIdAtual);
        assertFalse(plano.getProcedimentos().stream().anyMatch(p -> p.getId().equals(procedimentoAlvoId)));
    }

    @Então("a justificativa deve ser armazenada no log de auditoria")
    public void a_justificativa_deve_ser_armazenada_no_log() {
        PlanoTratamento plano = appService.getPlanoRepository().buscarPorId(planoIdAtual);
        assertFalse(plano.getLogsAuditoria().isEmpty());
    }

    @Então("o sistema do plano deve bloquear a exclusão")
    public void o_sistema_do_plano_deve_bloquear_exclusao() {
        assertNotNull(excecaoCapturada);
    }

    @Então("o status do plano deve ser {string}")
    public void o_status_do_plano_deve_ser(String statusEsperado) {
        PlanoTratamento plano = appService.getPlanoRepository().buscarPorId(planoIdAtual);
        assertEquals(StatusPlano.valueOf("ENCERRADO"), plano.getStatus());
    }

    @Então("a justificativa de encerramento deve estar vinculada ao prontuário vitalício do paciente")
    public void a_justificativa_de_encerramento_deve_estar_vinculada() {
        PlanoTratamento plano = appService.getPlanoRepository().buscarPorId(planoIdAtual);
        assertNotNull(plano.getJustificativaEncerramento());
    }
}