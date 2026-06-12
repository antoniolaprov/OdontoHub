package com.g4.odontohub.medicamento.domain.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultadoImportacao {

    private final List<String> sucessos = new ArrayList<>();
    private final Map<String, String> rejeicoes = new HashMap<>();

    public void registrarSucesso(String nomeComercial) {
        sucessos.add(nomeComercial);
    }

    public void registrarRejeicao(String nomeComercial, String motivo) {
        rejeicoes.put(nomeComercial, motivo);
    }

    public boolean foiImportado(String nomeComercial) {
        return sucessos.contains(nomeComercial);
    }

    public boolean foiRejeitado(String nomeComercial) {
        return rejeicoes.containsKey(nomeComercial);
    }

    public String motivoRejeicao(String nomeComercial) {
        return rejeicoes.get(nomeComercial);
    }
}
