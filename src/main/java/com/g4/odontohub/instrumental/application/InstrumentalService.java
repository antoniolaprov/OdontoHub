package com.g4.odontohub.instrumental.application;

import com.g4.odontohub.instrumental.domain.*;
import java.time.LocalDate;

public class InstrumentalService {

    private final InstrumentoRepository instrumentoRepository;
    private final CicloEsterilizacaoRepository cicloRepository;

    public InstrumentalService(InstrumentoRepository instrumentoRepository, CicloEsterilizacaoRepository cicloRepository) {
        this.instrumentoRepository = instrumentoRepository;
        this.cicloRepository = cicloRepository;
    }

    public void vincularInstrumento(String nomeInstrumento) {
        Instrumento instrumento = instrumentoRepository.buscarPorNome(nomeInstrumento);
        instrumento.vincularProcedimento();
        instrumentoRepository.salvar(instrumento);
    }

    public void registrarProcedimentoRealizado(String nomeInstrumento) {
        Instrumento instrumento = instrumentoRepository.buscarPorNome(nomeInstrumento);
        instrumento.marcarComoPendente();
        instrumentoRepository.salvar(instrumento);
    }

    public void registrarEsterilizacao(Long idCiclo, String nomeInstrumento, MetodoEsterilizacao metodo, LocalDate data, String responsavel) {
        Instrumento instrumento = instrumentoRepository.buscarPorNome(nomeInstrumento);
        
        CicloEsterilizacao ciclo = CicloEsterilizacao.criar(idCiclo, instrumento.getId(), metodo, data, responsavel);
        instrumento.atualizarEsterilizacao(ciclo.getValidade());
        
        cicloRepository.salvar(ciclo);
        instrumentoRepository.salvar(instrumento);
    }

    public void rotinaVerificarValidades(String nomeInstrumento, LocalDate dataVerificacao) {
        Instrumento instrumento = instrumentoRepository.buscarPorNome(nomeInstrumento);
        instrumento.verificarValidade(dataVerificacao);
        instrumentoRepository.salvar(instrumento);
    }
}