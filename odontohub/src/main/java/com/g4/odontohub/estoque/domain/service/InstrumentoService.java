package com.g4.odontohub.estoque.domain.service;

import com.g4.odontohub.estoque.domain.event.InstrumentoCadastrado;
import com.g4.odontohub.estoque.domain.event.InstrumentoContaminado;
import com.g4.odontohub.estoque.domain.event.InstrumentoDesativado;
import com.g4.odontohub.estoque.domain.event.InstrumentoEsterilizado;
import com.g4.odontohub.estoque.domain.event.InstrumentoVencido;
import com.g4.odontohub.estoque.domain.model.Instrumento;
import com.g4.odontohub.estoque.domain.model.InstrumentoId;
import com.g4.odontohub.estoque.domain.model.StatusEsterilizacao;
import com.g4.odontohub.estoque.domain.model.StatusInstrumento;
import com.g4.odontohub.shared.DomainEventPublisher;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InstrumentoService {

    private final Map<Long, Instrumento> repositorio = new HashMap<>();
    private long nextId = 1;
    private int prazoValidadeGlobalDias = 7;

    public Instrumento cadastrar(String nome, int prazoValidadeDias) {
        return cadastrarInstrumento(nome, "", "LEGACY-" + nextId, prazoValidadeDias, false);
    }

    public Instrumento cadastrarInstrumento(String nome, String categoria, String codigoIdentificador) {
        return cadastrarInstrumento(nome, categoria, codigoIdentificador, prazoValidadeGlobalDias, true);
    }

    public Instrumento cadastrarInstrumentoComPrazo(String nome, String categoria, String codigoIdentificador,
                                                    int prazoValidadeDias) {
        return cadastrarInstrumento(nome, categoria, codigoIdentificador, prazoValidadeDias, true);
    }

    public Instrumento cadastrarKit(String nome, String categoria, String codigoIdentificador,
                                    int prazoValidadeDias, List<String> codigosComponentes) {
        validarCadastro(nome, categoria, codigoIdentificador);
        if (existeCodigoIdentificador(codigoIdentificador)) {
            throw new IllegalArgumentException("Código já cadastrado. Use um código único.");
        }

        Instrumento instrumento = cadastrarInstrumento(
                nome, categoria, codigoIdentificador, prazoValidadeDias, false);
        instrumento.setTipo("KIT");
        instrumento.adicionarComponentes(new ArrayList<>(codigosComponentes));
        return instrumento;
    }

    private Instrumento cadastrarInstrumento(String nome, String categoria, String codigoIdentificador,
                                             int prazoValidadeDias, boolean validarObrigatorios) {
        if (validarObrigatorios) {
            validarCadastro(nome, categoria, codigoIdentificador);
        }
        if (existeCodigoIdentificador(codigoIdentificador)) {
            throw new IllegalArgumentException("Código já cadastrado. Use um código único.");
        }
        InstrumentoId id = new InstrumentoId(nextId++);
        Instrumento instrumento = new Instrumento(id, nome, categoria, codigoIdentificador, prazoValidadeDias);
        repositorio.put(id.id(), instrumento);
        DomainEventPublisher.publish(new InstrumentoCadastrado(id, nome, categoria, codigoIdentificador));
        return instrumento;
    }

    public void desativarInstrumento(Long instrumentoId) {
        Instrumento instrumento = buscarPorId(instrumentoId);
        instrumento.desativar();
        DomainEventPublisher.publish(new InstrumentoDesativado(instrumento.getId()));
    }

    public boolean existeCodigoIdentificador(String codigoIdentificador) {
        return repositorio.values().stream()
                .anyMatch(i -> i.getCodigoIdentificador().equals(codigoIdentificador));
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
        this.prazoValidadeGlobalDias = novoPrazoDias;
        repositorio.values().stream()
                .filter(i -> i.getStatusInstrumento() == StatusInstrumento.ATIVO)
                .forEach(i -> i.recalcularVencimento(novoPrazoDias));
    }

    public void recalcularValidadePorCategoria(String categoria, int novoPrazoDias) {
        repositorio.values().stream()
                .filter(i -> i.getStatusInstrumento() == StatusInstrumento.ATIVO)
                .filter(i -> i.getCategoria().equals(categoria))
                .forEach(i -> i.recalcularVencimento(novoPrazoDias));
    }

    public void configurarPrazoGlobal(int novoPrazoDias) {
        validarPrazo(novoPrazoDias);
        recalcularValidadeGlobal(novoPrazoDias);
    }

    public void configurarPrazoPorCategoria(String categoria, int novoPrazoDias) {
        validarPrazo(novoPrazoDias);
        recalcularValidadePorCategoria(categoria, novoPrazoDias);
    }

    public void verificarEAtualizarVencidos() {
        LocalDate hoje = LocalDate.now();
        repositorio.values().forEach(instrumento -> {
            if (instrumento.getStatus() == StatusEsterilizacao.ESTERIL
                    && instrumento.getStatusInstrumento() == StatusInstrumento.ATIVO
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
                        && i.getStatusInstrumento() == StatusInstrumento.ATIVO
                        && i.getDataVencimento() != null
                        && !hoje.isAfter(i.getDataVencimento()))
                .collect(Collectors.toList());
    }

    public List<Instrumento> listarInstrumentosAtivos() {
        return repositorio.values().stream()
                .filter(i -> i.getStatusInstrumento() == StatusInstrumento.ATIVO)
                .collect(Collectors.toList());
    }

    public Instrumento buscarPorNome(String nome) {
        return repositorio.values().stream()
                .filter(i -> i.getNome().equals(nome))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Instrumento não encontrado: " + nome));
    }

    public Instrumento buscarPorCodigo(String codigoIdentificador) {
        return repositorio.values().stream()
                .filter(i -> i.getCodigoIdentificador().equals(codigoIdentificador))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Instrumento não encontrado: " + codigoIdentificador));
    }

    private void validarCadastro(String nome, String categoria, String codigoIdentificador) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório.");
        }
        if (categoria == null || categoria.isBlank()) {
            throw new IllegalArgumentException("Categoria é obrigatória.");
        }
        if (codigoIdentificador == null || codigoIdentificador.isBlank()) {
            throw new IllegalArgumentException("Código é obrigatório.");
        }
    }

    private void validarPrazo(int novoPrazoDias) {
        if (novoPrazoDias < 1) {
            throw new IllegalArgumentException("Informe um número de dias válido (mínimo 1).");
        }
    }

    private Instrumento buscarPorId(Long id) {
        Instrumento instrumento = repositorio.get(id);
        if (instrumento == null) {
            throw new IllegalArgumentException("Instrumento não encontrado: " + id);
        }
        return instrumento;
    }
}
