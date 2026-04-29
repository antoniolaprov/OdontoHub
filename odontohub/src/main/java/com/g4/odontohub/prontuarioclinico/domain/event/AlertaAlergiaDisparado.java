package com.g4.odontohub.prontuarioclinico.domain.event;

public record AlertaAlergiaDisparado(Long pacienteId, String substanciaComRisco, String alergiaRegistrada) {}