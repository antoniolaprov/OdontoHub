package com.g4.odontohub.steps;

import com.g4.odontohub.prontuarioclinico.application.ProntuarioApplicationService;
import com.g4.odontohub.prontuarioclinico.domain.model.Anamnese;
import io.cucumber.java.pt.*;
import static org.junit.jupiter.api.Assertions.*;

public class F02_anamnese_Steps {

    private ProntuarioApplicationService appService;
    private Long pacienteIdAtual;
    private String nomeDentista = "Dr. Dentista";
    private Exception excecaoCapturada;
    private String alertaFarmacologico;

    @Dado("que o paciente {string} está cadastrado no sistema de prontuário")
    public void que_o_paciente_esta_cadastrado_no_sistema(String nomePaciente) {
        appService = new ProntuarioApplicationService();
        pacienteIdAtual = (long) nomePaciente.hashCode();
    }

    @Dado("que o paciente {string} não possui anamnese registrada")
    public void que_o_paciente_nao_possui_anamnese_registrada(String nomePaciente) {
        appService = new ProntuarioApplicationService();
        pacienteIdAtual = (long) nomePaciente.hashCode();
    }

    @Dado("que o paciente {string} possui anamnese com alergia a {string}")
    public void que_o_paciente_possui_anamnese_com_alergia_a(String nomePaciente, String alergia) {
        appService = new ProntuarioApplicationService();
        pacienteIdAtual = (long) nomePaciente.hashCode();
        // MUDANÇA AQUI: Adicionado a string vazia "" para a condição sistêmica
        appService.getAnamneseService().registrarAnamnese(pacienteIdAtual, alergia, "", "", nomeDentista);
    }

    @Dado("que o paciente {string} possui anamnese registrada na versão {int} com alergia a {string}")
    public void que_o_paciente_possui_anamnese_registrada_na_versao_com_alergia_a(String nomePaciente, Integer versao, String alergia) {
        appService = new ProntuarioApplicationService();
        pacienteIdAtual = (long) nomePaciente.hashCode();
        // MUDANÇA AQUI: Adicionado a string vazia "" para a condição sistêmica
        appService.getAnamneseService().registrarAnamnese(pacienteIdAtual, alergia, "", "", nomeDentista);
    }

    @Quando("o dentista registra a anamnese de {string} com alergia a {string} e condição sistêmica {string}")
    public void o_dentista_registra_a_anamnese_com_alergia_e_condicao(String nomePaciente, String alergia, String condicao) {
        // MUDANÇA AQUI: Agora a criação acontece em apenas UMA linha, garantindo a Versão 1!
        appService.getAnamneseService().registrarAnamnese(pacienteIdAtual, alergia, "", condicao, nomeDentista);
    }

    @Quando("o dentista tenta criar um Plano de Tratamento para {string}")
    public void o_dentista_tenta_criar_um_plano_de_tratamento(String nomePaciente) {
        try {
            appService.getPlanoTratamentoService().criarPlano(pacienteIdAtual, 999L);
        } catch (Exception e) {
            this.excecaoCapturada = e;
        }
    }

    @Quando("o dentista verifica a substância {string} para uso em procedimento de {string}")
    public void o_dentista_verifica_a_substancia_para_uso(String substancia, String nomePaciente) {
        this.alertaFarmacologico = appService.getAnamneseService()
            .verificarAlergiaParaSubstancia(pacienteIdAtual, substancia);
    }

    @Quando("o dentista adiciona a alergia {string} à anamnese de {string}")
    public void o_dentista_adiciona_a_alergia_a_anamnese(String novaAlergia, String nomePaciente) {
        appService.getAnamneseService().adicionarAlergia(pacienteIdAtual, novaAlergia, nomeDentista);
    }

    @Então("a anamnese deve ser salva com versão {int}")
    public void a_anamnese_deve_ser_salva_com_versao(Integer versaoEsperada) {
        Anamnese anamnese = appService.getRepository().buscarPorPacienteId(pacienteIdAtual);
        assertEquals(versaoEsperada, anamnese.getVersaoAtual());
    }

    @Então("a data de registro deve ser armazenada")
    public void a_data_de_registro_deve_ser_armazenada() {
        Anamnese anamnese = appService.getRepository().buscarPorPacienteId(pacienteIdAtual);
        assertNotNull(anamnese.getDataRegistro());
    }

    @Então("o responsável pelo cadastro deve ser registrado")
    public void o_responsavel_pelo_cadastro_deve_ser_registrado() {
        Anamnese anamnese = appService.getRepository().buscarPorPacienteId(pacienteIdAtual);
        assertNotNull(anamnese.getResponsavelCadastro());
    }

    @Então("o sistema deve bloquear a criação do plano")
    public void o_sistema_deve_bloquear_a_criacao_do_plano() {
        assertNotNull(excecaoCapturada);
    }

    @Então("a mensagem de erro do prontuário deve informar {string}")
    public void a_mensagem_de_erro_do_prontuario_deve_informar(String msgEsperada) {
        assertEquals(msgEsperada, excecaoCapturada.getMessage());
    }

    @Então("o sistema deve emitir um alerta de alergia")
    public void o_sistema_deve_emitir_um_alerta_de_alergia() {
        assertNotNull(this.alertaFarmacologico);
    }

    @Então("o alerta deve informar que {string} pertence à família farmacológica {string}")
    public void o_alerta_deve_informar_familia_farmacologica(String substancia, String familia) {
        assertTrue(this.alertaFarmacologico.contains(substancia));
        assertTrue(this.alertaFarmacologico.contains(familia));
    }

    @Então("a anamnese deve ser atualizada para a versão {int}")
    public void a_anamnese_deve_ser_atualizada_para_a_versao(Integer versaoEsperada) {
        Anamnese anamnese = appService.getRepository().buscarPorPacienteId(pacienteIdAtual);
        assertEquals(versaoEsperada, anamnese.getVersaoAtual());
    }

    @Então("o histórico deve conter a versão {int} com os dados anteriores")
    public void o_historico_deve_conter_a_versao_com_os_dados_anteriores(Integer versaoAnterior) {
        Anamnese anamnese = appService.getRepository().buscarPorPacienteId(pacienteIdAtual);
        assertTrue(anamnese.getHistoricoVersoes().stream().anyMatch(v -> v.versao() == versaoAnterior));
    }

    @Então("a data e o responsável pela atualização devem ser registrados")
    public void a_data_e_o_responsavel_pela_atualizacao_devem_ser_registrados() {
        Anamnese anamnese = appService.getRepository().buscarPorPacienteId(pacienteIdAtual);
        assertNotNull(anamnese.getDataUltimaAtualizacao());
        assertNotNull(anamnese.getResponsavelCadastro());
    }

    @Então("a anamnese deve conter as alergias {string} e {string}")
    public void a_anamnese_deve_conter_as_alergias_e(String alergia1, String alergia2) {
        Anamnese anamnese = appService.getRepository().buscarPorPacienteId(pacienteIdAtual);
        assertTrue(anamnese.getAlergias().contains(alergia1));
        assertTrue(anamnese.getAlergias().contains(alergia2));
    }
}