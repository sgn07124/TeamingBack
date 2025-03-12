package com.project.Teaming.domain.user.repository;

import com.project.Teaming.domain.mentoring.entity.MentoringParticipation;
import com.project.Teaming.domain.project.entity.ProjectParticipation;
import com.project.Teaming.domain.user.entity.Report;
import com.project.Teaming.domain.user.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface ReportRepository extends JpaRepository<Report, Long> {
    @Query("select r.reportedUser.id from Report r where r.projectParticipation = :projectParticipation and r.reportedUser.id in :userIds")
    Set<Long> findAllByProjectParticipationAndReportedUserIn(@Param("projectParticipation") ProjectParticipation projectParticipation,
                                                             @Param("userIds")List<Long> userIds);
    boolean existsByMentoringParticipationAndReportedUser(MentoringParticipation reporter, User reportedUser);

    boolean existsByProjectParticipationAndReportedUserId(ProjectParticipation projectParticipation, Long reportedUserId);

    // 처리되지 않은 신고 수 조회
    long countByReportedUserIdAndProjectParticipation_ProjectTeamIdAndWarningProcessedFalse(Long reportedUserId, Long teamId);

    // warningProcessed를 true로 업데이트
    @Modifying
    @Query("UPDATE Report r SET r.warningProcessed = true WHERE r.reportedUser.id = :reportedUserId AND r.projectParticipation.projectTeam.id = :teamId AND r.warningProcessed = false")
    void updateWarningProcessedByReportedUserAndTeamId(@Param("reportedUserId") Long reportedUserId, @Param("teamId") Long teamId);

    @Query("SELECT r.reportedUser.id " +
            "FROM Report r " +
            "WHERE r.mentoringParticipation.id = :currentParticipationId " +
            "AND r.reportedUser.id IN :userIds")
    Set<Long> findReportedUserIds(@Param("currentParticipationId") Long currentParticipationId,
                                  @Param("userIds") Set<Long> userIds);

    @Modifying
    @Query("DELETE FROM Report r WHERE r.mentoringParticipation = :mentoringParticipation")
    void deleteAllByMentoringParticipation(@Param("mentoringParticipation") MentoringParticipation mentoringParticipation);

    @Modifying
    @Query("UPDATE Report r SET r.projectParticipation = NULL WHERE r.projectParticipation.id = :participationId")
    void updateProjectParticipationNull(@Param("participationId") Long participationId);

    @Modifying
    @Query("UPDATE Report r SET r.mentoringParticipation = NULL WHERE r.mentoringParticipation.id = :participationId")
    void updateMentoringParticipationNull(@Param("participationId") Long participationId);

    @Modifying
    @Query("DELETE FROM Report r WHERE r.reportedUser.id = :userId")
    void deleteByReportedUserId(@Param("userId") Long userId);
}
