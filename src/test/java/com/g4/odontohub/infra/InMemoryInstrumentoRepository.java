package com.g4.odontohub.infra;

import com.g4.odontohub.instrumental.domain.Instrumento;
import com.g4.odontohub.instrumental.domain.InstrumentoRepository;
import java.util.HashMap;
import java.util.Map;

public class InMemoryInstrumentoRepository implements InstrumentoRepository {
    private final Map<String, Instrumento> banco = new HashMap<>();

    @Override
    public void salvar(Instrumento instrumento) {
        banco.put(instrumento.getNome(), instrumento);
    }

    @Override
    public Instrumento buscarPorNome(String nome) {
        return banco.get(nome);
    }
}