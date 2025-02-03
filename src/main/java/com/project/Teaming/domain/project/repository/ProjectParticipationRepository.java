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

    /**
     * 팀의 멤버 여부 판별 (현재 로그인 한 유저에 대하여)
     * @param projectTeamId 팀 id
     * @param userID 유저 id
     * @param status 팀에 수락된 상태
     * @param isDeleted 삭제되지 않음 >> 강퇴, 탈퇴 당하지 않음
     */
    boolean existsByProjectTeamIdAndUserIdAndParticipationStatusAndIsDeleted(Long projectTeamId, Long userID, ParticipationStatus status, boolean isDeleted);

    // 팀에 지원 여부
    boolean existsByProjectTeamIdAndUserIdAndDecisionDateIsNull(Long projectTeamId, Long userId);

    // 팀의 팀장 여부
    boolean existsByProjectTeamIdAndUserIdAndRole(Long projectTeamId, Long userId, ProjectRole role);

    Optional<ProjectParticipation> findByProjectTeamIdAndRole(Long projectId, ProjectRole role);

    List<ProjectParticipation> findByUserIdAndParticipationStatus(Long userId, ParticipationStatus status);

    /**
     * 현재 팀원 목록 조회
     * @param projectTeamId 팀 id
     * @param status 팀에 수락된 상태
     */
    List<ProjectParticipation> findByProjectTeamIdAndParticipationStatus(Long projectTeamId, ParticipationStatus status);

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
