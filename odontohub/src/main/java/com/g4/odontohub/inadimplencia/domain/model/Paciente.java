package com.g4.odontohub.inadimplencia.domain.model;

import java.util.UUID;

public class Paciente {

    private final String id;
    private final String nome;
    private StatusPaciente status;
    private boolean alertaRestricaoVisivel;

    public Paciente(String nome) {
        this.id = UUID.randomUUID().toString();
        this.nome = nome;
        this.status = StatusPaciente.REGULAR;
        this.alertaRestricaoVisivel = false;
    }

    public void aplicarRestricao() {
        this.status = StatusPaciente.RESTRITO;
        this.alertaRestricaoVisivel = true;
    }

    public void removerRestricao() {
        this.status = StatusPaciente.REGULAR;
        this.alertaRestricaoVisivel = false;
    }

    public boolean isRestrito() {
        return status == StatusPaciente.RESTRITO;
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public StatusPaciente getStatus() { return status; }
    public boolean isAlertaRestricaoVisivel() { return alertaRestricaoVisivel; }
}
