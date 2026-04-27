package com.g4.odontohub.inadimplencia.domain;

import lombok.Getter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Entidade de domínio: Registro de inadimplência de um paciente.
 * Agrega o histórico de tentativas de cobrança.
 */
@Getter
public class RegistroInadimplencia {

    private Long id;
    private String nomePaciente;
    private boolean bloqueado;
    private String dentistaAutorizador;
    private List<RegistroCobranca> historicoCobranca;

    private RegistroInadimplencia() {}

    public static RegistroInadimplencia criar(Long id, String nomePaciente) {
        RegistroInadimplencia r = new RegistroInadimplencia();
        r.id = id;
        r.nomePaciente = nomePaciente;
        r.bloqueado = true;
        r.historicoCobranca = new ArrayList<>();
        return r;
    }

    /**
     * Remove o bloqueio quando não há mais parcelas inadimplentes.
     */
    public void removerBloqueio() {
        this.bloqueado = false;
    }

    /**
     * Registra uma tentativa de cobrança no histórico.
     */
    public void registrarCobranca(LocalDate data, String responsavel, String resultado) {
        historicoCobranca.add(RegistroCobranca.criar(data, responsavel, resultado));
    }

    /**
     * Autoriza agendamento mediante identificação do dentista.
     */
    public void autorizarAgendamento(String nomeDentista) {
        this.dentistaAutorizador = nomeDentista;
    }

    public List<RegistroCobranca> getHistoricoCobranca() {
        return Collections.unmodifiableList(historicoCobranca);
    }
}
