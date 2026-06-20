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
import com.g4.odontohub.estoque.domain.repository.InstrumentoRepository;
import com.g4.odontohub.shared.DomainEventPublisher;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InstrumentoService {

    private final InstrumentoRepository repositorio;
    private int prazoValidadeGlobalDias = 7;

    public InstrumentoService(InstrumentoRepository repositorio) {
        this.repositorio = repositorio;
    }

    public Instrumento cadastrar(String nome, int prazoValidadeDias) {
        return cadastrarInstrumento(nome, "", "LEGACY-" + nome, prazoValidadeDias, false);
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
        repositorio.salvar(instrumento);
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
        InstrumentoId id = new InstrumentoId(repositorio.proximoId());
        Instrumento instrumento = new Instrumento(id, nome, categoria, codigoIdentificador, prazoValidadeDias);
        repositorio.salvar(instrumento);
        DomainEventPublisher.publish(new InstrumentoCadastrado(id, nome, categoria, codigoIdentificador));
        return instrumento;
    }

    public void desativarInstrumento(Long instrumentoId) {
        Instrumento instrumento = buscarPorId(instrumentoId);
        instrumento.desativar();
        repositorio.salvar(instrumento);
        DomainEventPublisher.publish(new InstrumentoDesativado(instrumento.getId()));
    }

    /** Único jeito de tirar um instrumento de Inativo — sem isso, desativar() é uma via de mão única. */
    public void reativarInstrumento(Long instrumentoId) {
        Instrumento instrumento = buscarPorId(instrumentoId);
        instrumento.reativar();
        repositorio.salvar(instrumento);
    }

    public boolean existeCodigoIdentificador(String codigoIdentificador) {
        return repositorio.todos().stream()
                .anyMatch(i -> i.getCodigoIdentificador().equals(codigoIdentificador));
    }

    public void marcarComoEsteril(Long instrumentoId, LocalDate dataEsterilizacao, String responsavel) {
        Instrumento instrumento = buscarPorId(instrumentoId);
        instrumento.marcarComoEsteril(dataEsterilizacao, responsavel);
        repositorio.salvar(instrumento);
        DomainEventPublisher.publish(new InstrumentoEsterilizado(
                instrumento.getId(), dataEsterilizacao, responsavel, instrumento.getDataVencimento()));
    }

    public void marcarComoContaminado(Long instrumentoId) {
        Instrumento instrumento = buscarPorId(instrumentoId);
        instrumento.marcarComoContaminado();
        repositorio.salvar(instrumento);
        DomainEventPublisher.publish(new InstrumentoContaminado(instrumento.getId()));
    }

    public void recalcularValidadeGlobal(int novoPrazoDias) {
        this.prazoValidadeGlobalDias = novoPrazoDias;
        repositorio.todos().stream()
                .filter(i -> i.getStatusInstrumento() == StatusInstrumento.ATIVO)
                .forEach(i -> {
                    i.recalcularVencimento(novoPrazoDias);
                    repositorio.salvar(i);
                });
    }

    public void recalcularValidadePorCategoria(String categoria, int novoPrazoDias) {
        repositorio.todos().stream()
                .filter(i -> i.getStatusInstrumento() == StatusInstrumento.ATIVO)
                .filter(i -> i.getCategoria().equals(categoria))
                .forEach(i -> {
                    i.recalcularVencimento(novoPrazoDias);
                    repositorio.salvar(i);
                });
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
        repositorio.todos().forEach(instrumento -> {
            if (instrumento.getStatus() == StatusEsterilizacao.ESTERIL
                    && instrumento.getStatusInstrumento() == StatusInstrumento.ATIVO
                    && instrumento.getDataVencimento() != null
                    && hoje.isAfter(instrumento.getDataVencimento())) {
                instrumento.marcarComoVencido();
                repositorio.salvar(instrumento);
                DomainEventPublisher.publish(new InstrumentoVencido(instrumento.getId(), instrumento.getNome()));
            }
        });
    }

    public List<Instrumento> listarEstereisDentroDoPrazo() {
        LocalDate hoje = LocalDate.now();
        return repositorio.todos().stream()
                .filter(i -> i.getStatus() == StatusEsterilizacao.ESTERIL
                        && i.getStatusInstrumento() == StatusInstrumento.ATIVO
                        && i.getDataVencimento() != null
                        && !hoje.isAfter(i.getDataVencimento()))
                .collect(Collectors.toList());
    }

    public List<Instrumento> listarInstrumentosAtivos() {
        return repositorio.todos().stream()
                .filter(i -> i.getStatusInstrumento() == StatusInstrumento.ATIVO)
                .collect(Collectors.toList());
    }

    public List<Instrumento> listarTodos() {
        return repositorio.todos();
    }

    public Instrumento buscarPorNome(String nome) {
        Instrumento i = repositorio.buscarPorNome(nome);
        if (i == null) {
            throw new IllegalArgumentException("Instrumento não encontrado: " + nome);
        }
        return i;
    }

    public Instrumento buscarPorCodigo(String codigoIdentificador) {
        Instrumento i = repositorio.buscarPorCodigo(codigoIdentificador);
        if (i == null) {
            throw new IllegalArgumentException("Instrumento não encontrado: " + codigoIdentificador);
        }
        return i;
    }

    public void salvar(Instrumento instrumento) {
        repositorio.salvar(instrumento);
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
        Instrumento instrumento = repositorio.buscarPorId(id);
        if (instrumento == null) {
            throw new IllegalArgumentException("Instrumento não encontrado: " + id);
        }
        return instrumento;
    }
}
