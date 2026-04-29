package com.g4.odontohub.prontuarioclinico.domain.service;

import com.g4.odontohub.prontuarioclinico.application.AnamneseRepository;
import com.g4.odontohub.prontuarioclinico.domain.event.*;
import com.g4.odontohub.prontuarioclinico.domain.model.*;

import java.util.ArrayList;
import java.util.List;

public class AnamneseService {
    
    private final AnamneseRepository repository;
    private final List<Object> domainEvents = new ArrayList<>();

    public AnamneseService(AnamneseRepository repository) {
        this.repository = repository;
    }

    // MUDANÇA AQUI: Assinatura agora recebe 'condicoesSistemicas'
    public void registrarAnamnese(Long pacienteId, String alergias, String contraindicacoes, String condicoesSistemicas, String responsavel) {
        if (anamneseExiste(pacienteId)) {
            throw new IllegalStateException("Paciente já possui anamnese registrada.");
        }
        
        AnamneseId id = new AnamneseId(System.currentTimeMillis());
        // Passando a condição diretamente para o construtor, garantindo que nasça na versão 1
        Anamnese anamnese = new Anamnese(id, new Anamnesepaciente(pacienteId), alergias, contraindicacoes, condicoesSistemicas, responsavel);
        repository.salvar(anamnese);
        
        domainEvents.add(new AnamneseRegistrada(id, pacienteId, responsavel));
    }

    public void adicionarAlergia(Long pacienteId, String novaAlergia, String responsavel) {
        Anamnese anamnese = repository.buscarPorPacienteId(pacienteId);
        if (anamnese != null) {
            anamnese.adicionarAlergia(novaAlergia, responsavel);
            repository.salvar(anamnese);
            domainEvents.add(new AnamneseAtualizada(anamnese.getId(), anamnese.getVersaoAtual(), responsavel));
        }
    }

    public void atualizarCondicoesSistemicas(Long pacienteId, String condicoes, String responsavel) {
        Anamnese anamnese = repository.buscarPorPacienteId(pacienteId);
        if (anamnese != null) {
            anamnese.atualizarCondicoesSistemicas(condicoes, responsavel);
            repository.salvar(anamnese);
            domainEvents.add(new AnamneseAtualizada(anamnese.getId(), anamnese.getVersaoAtual(), responsavel));
        }
    }

    public boolean anamneseExiste(Long pacienteId) {
        return repository.buscarPorPacienteId(pacienteId) != null;
    }

    public String verificarAlergiaParaSubstancia(Long pacienteId, String substancia) {
        Anamnese anamnese = repository.buscarPorPacienteId(pacienteId);
        if (anamnese == null) return null;

        if (substancia.equalsIgnoreCase("Amoxicilina") && anamnese.getAlergias().contains("Penicilina")) {
            domainEvents.add(new AlertaAlergiaDisparado(pacienteId, substancia, "Penicilina"));
            return substancia + " pertence à família farmacológica Penicilina";
        }
        return null;
    }

    public List<Object> getDomainEvents() {
        return domainEvents;
    }
}