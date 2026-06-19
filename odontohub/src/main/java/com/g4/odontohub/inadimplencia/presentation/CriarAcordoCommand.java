package com.g4.odontohub.inadimplencia.presentation;

/**
 * Comando de entrada para criação de acordo.
 * Representa os dados submetidos pela recepcionista via UI.
 */
public class CriarAcordoCommand {

    private final String pacienteId;
    private final int numeroParcelas;
    private final String justificativa;

    public CriarAcordoCommand(String pacienteId, int numeroParcelas, String justificativa) {
        if (pacienteId == null || pacienteId.isBlank())
            throw new IllegalArgumentException("pacienteId é obrigatório");
        if (numeroParcelas <= 0)
            throw new IllegalArgumentException("Número de parcelas deve ser maior que zero");
        if (justificativa == null || justificativa.isBlank())
            throw new IllegalArgumentException("Justificativa é obrigatória");

        this.pacienteId = pacienteId;
        this.numeroParcelas = numeroParcelas;
        this.justificativa = justificativa;
    }

    public String getPacienteId()    { return pacienteId; }
    public int getNumeroParcelas()   { return numeroParcelas; }
    public String getJustificativa() { return justificativa; }
}
