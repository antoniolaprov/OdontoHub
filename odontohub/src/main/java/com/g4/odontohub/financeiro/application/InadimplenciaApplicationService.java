package com.g4.odontohub.financeiro.application;

import com.g4.odontohub.financeiro.domain.model.*;
import com.g4.odontohub.financeiro.domain.service.InadimplenciaService;

import java.time.LocalDate;
import java.util.*;

public class InadimplenciaApplicationService {

    private final Map<Long, List<Parcela>> parcelasPorPaciente = new HashMap<>();
    private final Map<Long, Acordo> acordos = new HashMap<>();
    private final InadimplenciaService service = new InadimplenciaService();
    private long nextParcelaId = 1L;
    private long nextAcordoId = 1L;

    public void cadastrarPaciente(String nome) {
        // registro simbólico — ID fixo por nome para testes
    }

    public Parcela adicionarParcelaVencida(Long pacienteId, double valor, int diasAtraso) {
        LocalDate vencimento = LocalDate.now().minusDays(diasAtraso);
        Parcela p = new Parcela(nextParcelaId++, pacienteId, 1L, valor, vencimento);
        p.calcularJurosEMulta();
        parcelasPorPaciente.computeIfAbsent(pacienteId, k -> new ArrayList<>()).add(p);
        return p;
    }

    public boolean verificarRestricao(Long pacienteId) {
        List<Parcela> parcelas = parcelasPorPaciente.getOrDefault(pacienteId, List.of());
        return service.pacienteRestrito(parcelas);
    }

    public boolean verificarInadimplencia(Long pacienteId) {
        List<Parcela> parcelas = parcelasPorPaciente.getOrDefault(pacienteId, List.of());
        return service.pacienteInadimplente(parcelas);
    }

    public List<Parcela> consultarParcelas(Long pacienteId) {
        return parcelasPorPaciente.getOrDefault(pacienteId, List.of());
    }

    public Acordo firmarAcordo(Long pacienteId, int quantidadeNovasParcelas, double valorCadaNovaParcela) {
        List<Parcela> originais = new ArrayList<>(
                parcelasPorPaciente.getOrDefault(pacienteId, List.of()));

        List<Parcela> novas = new ArrayList<>();
        for (int i = 0; i < quantidadeNovasParcelas; i++) {
            Parcela nova = new Parcela(
                    nextParcelaId++, pacienteId, 1L,
                    valorCadaNovaParcela,
                    LocalDate.now().plusMonths(i + 1));
            novas.add(nova);
        }

        Acordo acordo = new Acordo(nextAcordoId++, pacienteId, originais, novas);
        acordos.put(acordo.getId().getId(), acordo);

        // atualiza repositório
        List<Parcela> todas = new ArrayList<>(originais);
        todas.addAll(novas);
        parcelasPorPaciente.put(pacienteId, todas);

        return acordo;
    }

    public void registrarInadimplenciaAcordo(Long acordoId) {
        Acordo acordo = acordos.get(acordoId);
        if (acordo == null)
            throw new IllegalArgumentException("Acordo não encontrado: " + acordoId);
        acordo.registrarInadimplencia();
    }

    public Acordo buscarAcordoPorPaciente(Long pacienteId) {
        return acordos.values().stream()
                .filter(a -> a.getPacienteId().equals(pacienteId))
                .findFirst()
                .orElse(null);
    }
}