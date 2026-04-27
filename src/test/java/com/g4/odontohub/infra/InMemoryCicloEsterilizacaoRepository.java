package com.g4.odontohub.infra;

import com.g4.odontohub.instrumental.domain.CicloEsterilizacao;
import com.g4.odontohub.instrumental.domain.CicloEsterilizacaoRepository;
import java.util.HashMap;
import java.util.Map;

public class InMemoryCicloEsterilizacaoRepository implements CicloEsterilizacaoRepository {
    private final Map<Long, CicloEsterilizacao> banco = new HashMap<>();

    @Override
    public void salvar(CicloEsterilizacao ciclo) {
        banco.put(ciclo.getInstrumentoId(), ciclo);
    }

    @Override
    public CicloEsterilizacao buscarUltimoPorInstrumento(Long instrumentoId) {
        return banco.get(instrumentoId);
    }
}