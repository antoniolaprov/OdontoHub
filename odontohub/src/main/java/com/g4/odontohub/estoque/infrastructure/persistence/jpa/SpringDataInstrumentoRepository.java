package com.g4.odontohub.estoque.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SpringDataInstrumentoRepository extends JpaRepository<InstrumentoJpaEntity, Long> {

    InstrumentoJpaEntity findByNome(String nome);

    InstrumentoJpaEntity findByCodigoIdentificador(String codigoIdentificador);

    @Query("select coalesce(max(i.id), 0) + 1 from InstrumentoJpaEntity i")
    long proximoId();
}
