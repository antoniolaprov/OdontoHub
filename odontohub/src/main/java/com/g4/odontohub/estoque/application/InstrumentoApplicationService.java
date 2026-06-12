package com.g4.odontohub.estoque.application;

import com.g4.odontohub.estoque.domain.model.Instrumento;
import com.g4.odontohub.estoque.domain.model.StatusEsterilizacao;
import com.g4.odontohub.estoque.domain.repository.InstrumentoRepository;
import com.g4.odontohub.estoque.domain.service.InstrumentoService;
import com.g4.odontohub.estoque.infrastructure.persistence.InMemoryInstrumentoRepository;

import java.time.LocalDate;
import java.util.List;

public class InstrumentoApplicationService {

    private final InstrumentoService service;

    public InstrumentoApplicationService() {
        this(new InMemoryInstrumentoRepository());
    }

    public InstrumentoApplicationService(InstrumentoRepository repositorio) {
        this.service = new InstrumentoService(repositorio);
    }

    public Instrumento cadastrar(String nome, int prazoValidadeDias) {
        return service.cadastrar(nome, prazoValidadeDias);
    }

    public Instrumento cadastrarInstrumento(String nome, String categoria, String codigoIdentificador) {
        return service.cadastrarInstrumento(nome, categoria, codigoIdentificador);
    }

    public Instrumento cadastrarInstrumentoComPrazo(String nome, String categoria, String codigoIdentificador,
                                                    int prazoValidadeDias) {
        return service.cadastrarInstrumentoComPrazo(nome, categoria, codigoIdentificador, prazoValidadeDias);
    }

    public Instrumento cadastrarKit(String nome, String categoria, String codigoIdentificador,
                                    int prazoValidadeDias, List<String> codigosComponentes) {
        return service.cadastrarKit(nome, categoria, codigoIdentificador, prazoValidadeDias, codigosComponentes);
    }

    public void desativarInstrumento(String nome) {
        service.desativarInstrumento(idDe(nome));
    }

    public boolean existeCodigoIdentificador(String codigoIdentificador) {
        return service.existeCodigoIdentificador(codigoIdentificador);
    }

    public void marcarComoEsteril(String nome, LocalDate dataEsterilizacao, String responsavel) {
        service.marcarComoEsteril(idDe(nome), dataEsterilizacao, responsavel);
    }

    public void marcarComoContaminado(String nome) {
        service.marcarComoContaminado(idDe(nome));
    }

    public void recalcularValidadeGlobal(int novoPrazoDias) {
        service.recalcularValidadeGlobal(novoPrazoDias);
    }

    public void configurarPrazoGlobal(int novoPrazoDias) {
        service.configurarPrazoGlobal(novoPrazoDias);
    }

    public void configurarPrazoPorCategoria(String categoria, int novoPrazoDias) {
        service.configurarPrazoPorCategoria(categoria, novoPrazoDias);
    }

    public void verificarEAtualizarVencidos() {
        service.verificarEAtualizarVencidos();
    }

    public List<Instrumento> listarEstereisDentroDoPrazo() {
        return service.listarEstereisDentroDoPrazo();
    }

    public List<Instrumento> listarInstrumentosAtivos() {
        return service.listarInstrumentosAtivos();
    }

    public Instrumento buscarPorNome(String nome) {
        return service.buscarPorNome(nome);
    }

    public Instrumento buscarPorCodigo(String codigoIdentificador) {
        return service.buscarPorCodigo(codigoIdentificador);
    }

    public void definirStatus(String nome, StatusEsterilizacao status, LocalDate dataVencimento) {
        Instrumento instrumento = service.buscarPorNome(nome);
        instrumento.setStatus(status);
        if (dataVencimento != null) {
            instrumento.setDataVencimento(dataVencimento);
        }
        service.salvar(instrumento);
    }

    private Long idDe(String nome) {
        return service.buscarPorNome(nome).getId().id();
    }
}
