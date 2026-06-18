package com.g4.odontohub.relacionamentopaciente.recall.domain.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Recall {

    /** Número de tentativas de contato sem sucesso a partir do qual o caso é escalonado. */
    private static final int LIMITE_ESCALONAMENTO = 3;

    /** SLA (em dias) para contato de casos críticos (prioridade ALTA) antes de gerar atraso. */
    private static final int SLA_DIAS_CRITICO = 2;

    private final RecallId id;
    private final String paciente;
    private final String procedimentoGatilho;
    private final int diasParaRetorno;
    private final NivelPrioridade prioridade;
    private StatusRecall status;
    private boolean flagConversaoRecall;
    private int tentativasContato;
    private CategoriaRecall categoria;
    private FatoresPriorizacao fatores;
    private LocalDate dataCriacao;
    private MotivoExclusao motivoExclusao;
    private final List<TentativaContato> historicoTentativas = new ArrayList<>();

    public Recall(RecallId id, String paciente, String procedimentoGatilho, int diasParaRetorno) {
        this.id = id;
        this.paciente = paciente;
        this.procedimentoGatilho = procedimentoGatilho;
        this.diasParaRetorno = diasParaRetorno;
        this.prioridade = NivelPrioridade.doProcedimento(procedimentoGatilho);
        this.status = StatusRecall.NA_FILA;
        this.fatores = FatoresPriorizacao.vazio();
        this.dataCriacao = LocalDate.now();
        this.categoria = classificarCategoria();
    }

    /** Reconstitui o agregado a partir da persistência (camada de infraestrutura). */
    public static Recall reconstituir(RecallId id, String paciente, String procedimentoGatilho,
                                      int diasParaRetorno, StatusRecall status, boolean flagConversaoRecall,
                                      int tentativasContato, CategoriaRecall categoria, FatoresPriorizacao fatores,
                                      LocalDate dataCriacao, MotivoExclusao motivoExclusao,
                                      List<TentativaContato> historicoTentativas) {
        Recall r = new Recall(id, paciente, procedimentoGatilho, diasParaRetorno);
        r.status = status;
        r.flagConversaoRecall = flagConversaoRecall;
        r.tentativasContato = tentativasContato;
        r.fatores = fatores == null ? FatoresPriorizacao.vazio() : fatores;
        r.categoria = categoria == null ? r.classificarCategoria() : categoria;
        r.dataCriacao = dataCriacao;
        r.motivoExclusao = motivoExclusao;
        if (historicoTentativas != null) {
            r.historicoTentativas.addAll(historicoTentativas);
        }
        return r;
    }

    public void cancelar() {
        this.status = StatusRecall.CANCELADO;
    }

    public void converter() {
        this.status = StatusRecall.CONVERTIDO;
        this.flagConversaoRecall = true;
    }

    /** Exclui o paciente do recall automaticamente (falecido, transferido, alta, judicial). */
    public void excluir(MotivoExclusao motivo) {
        this.status = StatusRecall.EXCLUIDO;
        this.motivoExclusao = motivo;
    }

    /** Atualiza os fatores de priorização e reclassifica a categoria (enquanto na fila). */
    public void definirFatores(FatoresPriorizacao novosFatores) {
        this.fatores = novosFatores == null ? FatoresPriorizacao.vazio() : novosFatores;
        if (status == StatusRecall.NA_FILA) {
            this.categoria = classificarCategoria();
        }
    }

    /** Registra uma tentativa de contato simples (SLA). Retorna true se deve escalonar. */
    public boolean registrarTentativaContato() {
        this.tentativasContato++;
        return deveEscalonar();
    }

    /**
     * Registra uma tentativa de contato detalhada (canal, duração, resultado, responsável).
     * Resultados sem sucesso contam para o escalonamento; "Retorno agendado" muda o status.
     * Retorna true se o caso deve ser escalonado.
     */
    public boolean registrarTentativa(TentativaContato tentativa) {
        this.historicoTentativas.add(tentativa);
        if (tentativa.resultado() == ResultadoContato.RETORNO_AGENDADO) {
            this.status = StatusRecall.AGENDADO;
            return false;
        }
        if (tentativa.resultado().semSucesso()) {
            this.tentativasContato++;
        }
        return deveEscalonar();
    }

    /** Caso ainda na fila que atingiu o limite de tentativas precisa de escalonamento. */
    public boolean deveEscalonar() {
        return status == StatusRecall.NA_FILA && tentativasContato >= LIMITE_ESCALONAMENTO;
    }

    /**
     * Pontuação do motor de priorização inteligente (F07): combina o risco clínico do
     * procedimento (base) com os demais fatores comportamentais e financeiros.
     */
    public int pontuacaoPrioridade() {
        int score = prioridade.peso() * 10;
        score += Math.min(fatores.mesesSemRetorno(), 24) * 2;
        score += fatores.tratamentosPendentes() * 5;
        score += fatores.procedimentoCritico() ? 15 : 0;
        score += fatores.inadimplente() ? 10 : 0;
        score += fatores.historicoFaltas() * 3;
        score += fatores.cancelamentosRecorrentes() * 4;
        score += fatores.riscoClinicoAlto() ? 20 : 0;
        score += fatores.potencialFinanceiro() >= 1000 ? 10 : 0;
        score += fatores.tratamentoInterrompido() ? 12 : 0;
        return score;
    }

    /** Indicador visual automático do paciente na fila (F07). */
    public IndicadorVisual indicadorVisual() {
        if (categoria == CategoriaRecall.TRATAMENTO_INTERROMPIDO || deveEscalonar()) {
            return IndicadorVisual.VERMELHO;
        }
        if (fatores.inadimplente()) {
            return IndicadorVisual.ROXO;
        }
        if (categoria == CategoriaRecall.POS_OPERATORIO) {
            return IndicadorVisual.AZUL;
        }
        if (tentativasContato > 0 || fatores.mesesSemRetorno() >= 6) {
            return IndicadorVisual.AMARELO;
        }
        return IndicadorVisual.VERDE;
    }

    /** SLA da fila: casos críticos (ALTA) não contatados dentro do prazo geram atraso. */
    public boolean slaVencido(LocalDate hoje) {
        return status == StatusRecall.NA_FILA
                && prioridade == NivelPrioridade.ALTA
                && dataCriacao != null
                && ChronoUnit.DAYS.between(dataCriacao, hoje) > SLA_DIAS_CRITICO;
    }

    private CategoriaRecall classificarCategoria() {
        if (fatores.pacienteEvadido()) {
            return CategoriaRecall.REATIVACAO;
        }
        if (fatores.tratamentoInterrompido()) {
            return CategoriaRecall.TRATAMENTO_INTERROMPIDO;
        }
        if (fatores.inadimplente()) {
            return CategoriaRecall.FINANCEIRO;
        }
        if (ehPosOperatorio(procedimentoGatilho)) {
            return CategoriaRecall.POS_OPERATORIO;
        }
        return CategoriaRecall.PREVENTIVO;
    }

    private static boolean ehPosOperatorio(String procedimento) {
        String p = procedimento == null ? "" : procedimento.trim().toUpperCase();
        return p.equals("CIRURGIA") || p.equals("IMPLANTE");
    }

    public RecallId getId() { return id; }
    public String getPaciente() { return paciente; }
    public String getProcedimentoGatilho() { return procedimentoGatilho; }
    public int getDiasParaRetorno() { return diasParaRetorno; }
    public NivelPrioridade getPrioridade() { return prioridade; }
    public StatusRecall getStatus() { return status; }
    public boolean isFlagConversaoRecall() { return flagConversaoRecall; }
    public int getTentativasContato() { return tentativasContato; }
    public CategoriaRecall getCategoria() { return categoria; }
    public FatoresPriorizacao getFatores() { return fatores; }
    public LocalDate getDataCriacao() { return dataCriacao; }
    public MotivoExclusao getMotivoExclusao() { return motivoExclusao; }
    public List<TentativaContato> getHistoricoTentativas() { return Collections.unmodifiableList(historicoTentativas); }
}
