package com.project.Teaming.domain.project.repository;

import com.project.Teaming.domain.project.entity.ParticipationStatus;
import com.project.Teaming.domain.project.entity.ProjectParticipation;
import com.project.Teaming.domain.project.entity.ProjectRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectParticipationRepository extends JpaRepository<ProjectParticipation, Long> {

    Optional<ProjectParticipation> findByProjectTeamIdAndUserId(Long projectId, Long userId);

    List<ProjectParticipation> findByProjectTeamId(Long teamId);

    boolean existsByProjectTeamIdAndUserIdAndParticipationStatus(Long projectTeamId, Long userID, ParticipationStatus status);

    Optional<ProjectParticipation> findByProjectTeamIdAndRole(Long projectId, ProjectRole role);

    List<ProjectParticipation> findByUserIdAndParticipationStatus(Long userId, ParticipationStatus status);

    List<ProjectParticipation> findByProjectTeamIdAndParticipationStatus(Long projectTeamId, ParticipationStatus status);

    Optional<ProjectParticipation> findByProjectTeamIdAndUserIdAndParticipationStatus(Long projectId, Long userId, ParticipationStatus status);

    long countByProjectTeamIdAndParticipationStatusAndIsDeleted(Long teamId, ParticipationStatus status, boolean delete);

    @Query("SELECT pp FROM ProjectParticipation pp JOIN pp.projectTeam pt "
            + "WHERE pt.id = :teamId AND pp.isDeleted = false AND pp.participationStatus = :participationStatus AND pp.role = :role "
            + "ORDER BY pp.decisionDate ASC ")
    List<ProjectParticipation> findTeamUsers(
            @Param("teamId") Long teamId,
            @Param("participationStatus") ParticipationStatus participationStatus,
            @Param("role") ProjectRole role
    );
}
