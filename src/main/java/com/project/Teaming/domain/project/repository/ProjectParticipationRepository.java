package com.project.Teaming.domain.project.repository;

import com.project.Teaming.domain.project.entity.ProjectParticipation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectParticipationRepository extends JpaRepository<ProjectParticipation, Long> {

    Optional<ProjectParticipation> findByProjectTeamId(Long projectId);

    Optional<ProjectParticipation> findByProjectTeamIdAndUserId(Long projectId, Long userId);
}
