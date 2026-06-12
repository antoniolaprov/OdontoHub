package com.g4.odontohub.agendamento.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SpringDataAgendamentoRepository extends JpaRepository<AgendamentoJpaEntity, Long> {

    @Query("select coalesce(max(a.id), 0) + 1 from AgendamentoJpaEntity a")
    long proximoId();
}
