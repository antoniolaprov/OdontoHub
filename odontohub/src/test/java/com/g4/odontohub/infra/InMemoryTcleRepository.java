package com.g4.odontohub.infra;

import com.g4.odontohub.tratamento.domain.StatusTcle;
import com.g4.odontohub.tratamento.domain.Tcle;
import com.g4.odontohub.tratamento.domain.TcleRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Implementação in-memory do TcleRepository para uso exclusivo nos testes BDD.
 */
public class InMemoryTcleRepository implements TcleRepository {

    private final Map<Long, Tcle> banco = new HashMap<>();

    @Override
    public void salvar(Tcle tcle) {
        banco.put(tcle.getId(), tcle);
    }

    @Override
    public Optional<Tcle> buscarPorId(Long id) {
        return Optional.ofNullable(banco.get(id));
    }

    @Override
    public Optional<Tcle> buscarAssinadoPorPlano(Long planoId) {
        return banco.values().stream()
                .filter(t -> t.getPlanoId().equals(planoId) && t.getStatus() == StatusTcle.ASSINADO)
                .findFirst();
    }

    @Override
    public void deletar(Long id) {
        banco.remove(id);
    }
}
