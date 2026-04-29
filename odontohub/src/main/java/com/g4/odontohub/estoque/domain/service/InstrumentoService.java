package com.g4.odontohub.estoque.domain.service;

import com.g4.odontohub.estoque.domain.event.InstrumentoContaminado;
import com.g4.odontohub.estoque.domain.event.InstrumentoEsterilizado;
import com.g4.odontohub.estoque.domain.event.InstrumentoVencido;
import com.g4.odontohub.estoque.domain.model.Instrumento;
import com.g4.odontohub.estoque.domain.model.InstrumentoId;
import com.g4.odontohub.estoque.domain.model.StatusEsterilizacao;
import com.g4.odontohub.shared.DomainEventPublisher;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InstrumentoService {

    private final Map<Long, Instrumento> repositorio = new HashMap<>();
    private long nextId = 1;

    public Instrumento cadastrar(String nome, int prazoValidadeDias) {
        InstrumentoId id = new InstrumentoId(nextId++);
        Instrumento instrumento = new Instrumento(id, nome, prazoValidadeDias);
        repositorio.put(id.id(), instrumento);
        return instrumento;
    }

    public void marcarComoEsteril(Long instrumentoId, LocalDate dataEsterilizacao, String responsavel) {
        Instrumento instrumento = buscarPorId(instrumentoId);
        instrumento.marcarComoEsteril(dataEsterilizacao, responsavel);
        DomainEventPublisher.publish(new InstrumentoEsterilizado(
                instrumento.getId(),
                dataEsterilizacao,
                responsavel,
                instrumento.getDataVencimento()
        ));
    }

    public void marcarComoContaminado(Long instrumentoId) {
        Instrumento instrumento = buscarPorId(instrumentoId);
        instrumento.marcarComoContaminado();
        DomainEventPublisher.publish(new InstrumentoContaminado(instrumento.getId()));
    }

    public void recalcularValidadeGlobal(int novoPrazoDias) {
        repositorio.values().forEach(i -> i.recalcularVencimento(novoPrazoDias));
    }

    public void verificarEAtualizarVencidos() {
        LocalDate hoje = LocalDate.now();
        repositorio.values().forEach(instrumento -> {
            if (instrumento.getStatus() == StatusEsterilizacao.ESTERIL
                    && instrumento.getDataVencimento() != null
                    && hoje.isAfter(instrumento.getDataVencimento())) {
                instrumento.marcarComoVencido();
                DomainEventPublisher.publish(new InstrumentoVencido(instrumento.getId(), instrumento.getNome()));
            }
        });
    }

    public List<Instrumento> listarEstereisDentroDoPrazo() {
        LocalDate hoje = LocalDate.now();
        return repositorio.values().stream()
                .filter(i -> i.getStatus() == StatusEsterilizacao.ESTERIL
                        && i.getDataVencimento() != null
                        && !hoje.isAfter(i.getDataVencimento()))
                .collect(Collectors.toList());
    }

    public Instrumento buscarPorNome(String nome) {
        return repositorio.values().stream()
                .filter(i -> i.getNome().equals(nome))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Instrumento não encontrado: " + nome));
    }

    private Instrumento buscarPorId(Long id) {
        Instrumento instrumento = repositorio.get(id);
        if (instrumento == null) throw new IllegalArgumentException("Instrumento não encontrado: " + id);
        return instrumento;
    }
}