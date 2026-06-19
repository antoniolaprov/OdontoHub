package com.g4.odontohub.inadimplencia.presentation;

/**
 * Comando de entrada para registro de tentativa de cobrança.
 */
public class RegistrarCobrancaCommand {

    private final String pacienteId;
    private final String responsavel;
    private final String canal;
    private final String resultado;
    private final String observacao;

    public RegistrarCobrancaCommand(String pacienteId, String responsavel,
                                     String canal, String resultado, String observacao) {
        this.pacienteId  = pacienteId;
        this.responsavel = responsavel;
        this.canal       = canal;
        this.resultado   = resultado;
        this.observacao  = observacao;
    }

    public String getPacienteId()  { return pacienteId; }
    public String getResponsavel() { return responsavel; }
    public String getCanal()       { return canal; }
    public String getResultado()   { return resultado; }
    public String getObservacao()  { return observacao; }
}
