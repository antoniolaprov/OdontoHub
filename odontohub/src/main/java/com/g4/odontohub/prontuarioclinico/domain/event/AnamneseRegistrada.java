package com.g4.odontohub.prontuarioclinico.domain.event;

import com.g4.odontohub.prontuarioclinico.domain.model.AnamneseId;

public record AnamneseRegistrada(AnamneseId anamneseId, Long pacienteId, String responsavel) {}