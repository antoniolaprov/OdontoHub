package com.g4.odontohub.confirmacao.naocomparecimento.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SpringDataNaoComparecimentoRepository extends JpaRepository<NaoComparecimentoJpaEntity, Long> {

    NaoComparecimentoJpaEntity findFirstByAgendamentoId(Long agendamentoId);

    List<NaoComparecimentoJpaEntity> findByPaciente(String paciente);

    @Query("select coalesce(max(n.id), 0) + 1 from NaoComparecimentoJpaEntity n")
    long proximoId();
}
