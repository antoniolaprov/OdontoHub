package com.g4.odontohub.prontuarioclinico.domain.model;

import java.util.Date;

public class VOsPlano {
    public record Planotratamentopaciente(Long pacienteId) {}
    public record Planotratamentodentista(Long dentistaId) {}
    public record ProcedimentoAgendamentoVinculado(Long agendamentoVinculadoId) {}
    public record PlanoId(Long id) {}
    public record ProcedimentoId(Long id) {}
    public record EvolucaoClinica(String descricaoTecnica, String executor, Date dataRegistro) {}
    public record LogAuditoria(String acao, String justificativa, String responsavel, Date dataAcao) {}
}