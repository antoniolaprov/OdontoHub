package com.g4.odontohub.medicamento.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SpringDataMedicamentoRepository extends JpaRepository<MedicamentoJpaEntity, Long> {

    MedicamentoJpaEntity findByNomeComercial(String nomeComercial);

    @Query("select coalesce(max(m.id), 0) + 1 from MedicamentoJpaEntity m")
    long proximoId();
}
