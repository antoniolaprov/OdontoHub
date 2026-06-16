package com.g4.odontohub.confirmacao.lembrete.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SpringDataLembreteRepository extends JpaRepository<LembreteJpaEntity, Long> {

    LembreteJpaEntity findFirstByAgendamentoIdOrderByIdDesc(Long agendamentoId);

    @Query("select coalesce(max(l.id), 0) + 1 from LembreteJpaEntity l")
    long proximoId();
}
