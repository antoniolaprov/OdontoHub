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

    public void marcarComoEsteril(String nome, LocalDate dataEsterilizacao, String responsavel) {
        service.marcarComoEsteril(resolverIdPorNome(nome), dataEsterilizacao, responsavel);
    }

    public void marcarComoContaminado(String nome) {
        service.marcarComoContaminado(resolverIdPorNome(nome));
    }

    public void recalcularValidadeGlobal(int novoPrazoDias) {
        service.recalcularValidadeGlobal(novoPrazoDias);
    }

    public void verificarEAtualizarVencidos() {
        service.verificarEAtualizarVencidos();
    }

    public List<Instrumento> listarEstereisDentroDoPrazo() {
        return service.listarEstereisDentroDoPrazo();
    }

    public Instrumento buscarPorNome(String nome) {
        return service.buscarPorNome(nome);
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
        if (id == null) throw new IllegalArgumentException("Instrumento não cadastrado: " + nome);
        return id;
    }
}