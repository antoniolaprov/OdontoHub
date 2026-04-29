package com.g4.odontohub.prontuarioclinico.domain.event;

import com.g4.odontohub.prontuarioclinico.domain.model.AnamneseId;

public record AnamneseAtualizada(AnamneseId anamneseId, int novaVersao, String responsavel) {}