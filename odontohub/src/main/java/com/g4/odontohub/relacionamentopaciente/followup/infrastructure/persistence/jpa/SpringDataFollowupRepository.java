package com.g4.odontohub.relacionamentopaciente.followup.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SpringDataFollowupRepository extends JpaRepository<FollowupJpaEntity, Long> {

    @Query("select coalesce(max(f.id), 0) + 1 from FollowupJpaEntity f")
    long proximoId();
}
