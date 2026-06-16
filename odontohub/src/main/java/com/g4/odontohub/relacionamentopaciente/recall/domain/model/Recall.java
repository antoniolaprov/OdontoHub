package com.g4.odontohub.relacionamentopaciente.recall.domain.model;

public class Recall {

    /** Número de tentativas de contato sem sucesso a partir do qual o caso é escalonado. */
    private static final int LIMITE_ESCALONAMENTO = 3;

    private final RecallId id;
    private final String paciente;
    private final String procedimentoGatilho;
    private final int diasParaRetorno;
    private final NivelPrioridade prioridade;
    private StatusRecall status;
    private boolean flagConversaoRecall;
    private int tentativasContato;

    public Recall(RecallId id, String paciente, String procedimentoGatilho, int diasParaRetorno) {
        this.id = id;
        this.paciente = paciente;
        this.procedimentoGatilho = procedimentoGatilho;
        this.diasParaRetorno = diasParaRetorno;
        this.prioridade = NivelPrioridade.doProcedimento(procedimentoGatilho);
        this.status = StatusRecall.NA_FILA;
    }

    /** Reconstitui o agregado a partir da persistência (camada de infraestrutura). */
    public static Recall reconstituir(RecallId id, String paciente, String procedimentoGatilho,
                                      int diasParaRetorno, StatusRecall status, boolean flagConversaoRecall,
                                      int tentativasContato) {
        Recall r = new Recall(id, paciente, procedimentoGatilho, diasParaRetorno);
        r.status = status;
        r.flagConversaoRecall = flagConversaoRecall;
        r.tentativasContato = tentativasContato;
        return r;
    }

    public void cancelar() {
        this.status = StatusRecall.CANCELADO;
    }

    public void converter() {
        this.status = StatusRecall.CONVERTIDO;
        this.flagConversaoRecall = true;
    }

    /** Registra uma tentativa de contato (SLA). Retorna true quando o caso deve ser escalonado. */
    public boolean registrarTentativaContato() {
        this.tentativasContato++;
        return deveEscalonar();
    }

    /** Caso ainda na fila que atingiu o limite de tentativas precisa de escalonamento. */
    public boolean deveEscalonar() {
        return status == StatusRecall.NA_FILA && tentativasContato >= LIMITE_ESCALONAMENTO;
    }

    public RecallId getId() { return id; }
    public String getPaciente() { return paciente; }
    public String getProcedimentoGatilho() { return procedimentoGatilho; }
    public int getDiasParaRetorno() { return diasParaRetorno; }
    public NivelPrioridade getPrioridade() { return prioridade; }
    public StatusRecall getStatus() { return status; }
    public boolean isFlagConversaoRecall() { return flagConversaoRecall; }
    public int getTentativasContato() { return tentativasContato; }
}
