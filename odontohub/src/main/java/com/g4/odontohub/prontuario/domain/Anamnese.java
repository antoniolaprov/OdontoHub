package com.g4.odontohub.prontuario.domain;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.*;

@Getter
public class Anamnese {

    // Mapeamento de substâncias para sua família farmacológica (regra de domínio)
    private static final Map<String, String> FAMILIAS_FARMACOLOGICAS = Map.of(
            "amoxicilina", "Penicilina",
            "ampicilina",  "Penicilina",
            "penicilina",  "Penicilina"
    );

    private Long pacienteId;
    private List<String> alergias;
    private List<String> contraindicacoes;
    private String condicoesSistemicas;
    private int versao;
    private String responsavel;
    private LocalDateTime dataRegistro;
    private LocalDateTime dataUltimaAtualizacao;
    private List<VersaoAnamnese> historico;

    private Anamnese() {}

    // ── Fábrica ──────────────────────────────────────────────────────────────

    public static Anamnese criar(Long pacienteId,
                                 List<String> alergias,
                                 List<String> contraindicacoes,
                                 String responsavel) {
        Anamnese a = new Anamnese();
        a.pacienteId = pacienteId;
        a.alergias = new ArrayList<>(alergias);
        a.contraindicacoes = new ArrayList<>(contraindicacoes);
        a.condicoesSistemicas = null;
        a.versao = 1;
        a.responsavel = responsavel;
        a.dataRegistro = LocalDateTime.now();
        a.dataUltimaAtualizacao = a.dataRegistro;
        a.historico = new ArrayList<>();
        return a;
    }

    // ── Comportamentos ───────────────────────────────────────────────────────

    public void adicionarAlergia(String novaAlergia, String responsavel) {
        salvarVersaoNoHistorico(responsavel);
        this.alergias.add(novaAlergia);
        this.responsavel = responsavel;
        this.versao++;
        this.dataUltimaAtualizacao = LocalDateTime.now();
    }

    public void atualizarCondicoesSistemicas(String novasCondicoes, String responsavel) {
        salvarVersaoNoHistorico(responsavel);
        this.condicoesSistemicas = novasCondicoes;
        this.responsavel = responsavel;
        this.versao++;
        this.dataUltimaAtualizacao = LocalDateTime.now();
    }

    // Retorna a alergia conflitante, se houver, levando em conta famílias farmacológicas
    public Optional<String> verificarAlergiaParaSubstancia(String substanciaUtilizada) {
        Optional<String> direta = alergias.stream()
                .filter(a -> a.equalsIgnoreCase(substanciaUtilizada))
                .findFirst();
        if (direta.isPresent()) return direta;

        String familia = FAMILIAS_FARMACOLOGICAS.get(substanciaUtilizada.toLowerCase());
        if (familia != null) {
            return alergias.stream()
                    .filter(a -> a.equalsIgnoreCase(familia)
                            || familia.equalsIgnoreCase(
                                    FAMILIAS_FARMACOLOGICAS.getOrDefault(a.toLowerCase(), "")))
                    .findFirst();
        }
        return Optional.empty();
    }

    // ── Getters com proteção de imutabilidade ────────────────────────────────

    public List<String> getAlergias() {
        return Collections.unmodifiableList(alergias);
    }

    public List<String> getContraindicacoes() {
        return Collections.unmodifiableList(contraindicacoes);
    }

    public List<VersaoAnamnese> getHistorico() {
        return Collections.unmodifiableList(historico);
    }

    // ── Helpers privados ─────────────────────────────────────────────────────

    private void salvarVersaoNoHistorico(String responsavel) {
        historico.add(new VersaoAnamnese(
                versao,
                new ArrayList<>(alergias),
                new ArrayList<>(contraindicacoes),
                condicoesSistemicas,
                LocalDateTime.now(),
                responsavel));
    }
}
