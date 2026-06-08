package com.g4.odontohub.estoque.application;

import com.g4.odontohub.estoque.domain.model.Instrumento;
import com.g4.odontohub.estoque.domain.model.StatusEsterilizacao;
import com.g4.odontohub.estoque.domain.service.InstrumentoService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstrumentoApplicationService {

    private final InstrumentoService service = new InstrumentoService();
    private final Map<String, Long> instrumentoIds = new HashMap<>();

    public Instrumento cadastrar(String nome, int prazoValidadeDias) {
        Instrumento instrumento = service.cadastrar(nome, prazoValidadeDias);
        instrumentoIds.put(nome, instrumento.getId().id());
        return instrumento;
    }

    public Instrumento cadastrarInstrumento(String nome, String categoria, String codigoIdentificador) {
        Instrumento instrumento = service.cadastrarInstrumento(nome, categoria, codigoIdentificador);
        instrumentoIds.put(nome, instrumento.getId().id());
        return instrumento;
    }

    public Instrumento cadastrarInstrumentoComPrazo(String nome, String categoria, String codigoIdentificador,
                                                    int prazoValidadeDias) {
        Instrumento instrumento = service.cadastrarInstrumentoComPrazo(
                nome, categoria, codigoIdentificador, prazoValidadeDias);
        instrumentoIds.put(nome, instrumento.getId().id());
        return instrumento;
    }

    public Instrumento cadastrarKit(String nome, String categoria, String codigoIdentificador,
                                    int prazoValidadeDias, List<String> codigosComponentes) {
        Instrumento instrumento = service.cadastrarKit(
                nome, categoria, codigoIdentificador, prazoValidadeDias, codigosComponentes);
        instrumentoIds.put(nome, instrumento.getId().id());
        return instrumento;
    }

    public void desativarInstrumento(String nome) {
        service.desativarInstrumento(resolverIdPorNome(nome));
    }

    public boolean existeCodigoIdentificador(String codigoIdentificador) {
        return service.existeCodigoIdentificador(codigoIdentificador);
    }

    public void marcarComoEsteril(String nome, LocalDate dataEsterilizacao, String responsavel) {
        service.marcarComoEsteril(resolverIdPorNome(nome), dataEsterilizacao, responsavel);
    }

    public void marcarComoContaminado(String nome) {
        service.marcarComoContaminado(resolverIdPorNome(nome));
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
    }

    private Long resolverIdPorNome(String nome) {
        Long id = instrumentoIds.get(nome);
        if (id == null) {
            throw new IllegalArgumentException("Instrumento não cadastrado: " + nome);
        }
        return id;
    }
}
