package com.project.Teaming.domain.mentoring.repository;

import com.project.Teaming.domain.mentoring.dto.response.RsTeamParticipationDto;
import com.project.Teaming.domain.mentoring.dto.response.RsTeamUserDto;
import com.project.Teaming.domain.mentoring.dto.response.RsUserParticipationDto;
import com.project.Teaming.domain.mentoring.entity.MentoringAuthority;
import com.project.Teaming.domain.mentoring.entity.MentoringParticipation;
import com.project.Teaming.domain.mentoring.entity.MentoringParticipationStatus;
import com.project.Teaming.domain.mentoring.entity.MentoringTeam;
import com.project.Teaming.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;

import java.util.List;
import java.util.Optional;

public interface MentoringParticipationRepository extends JpaRepository<MentoringParticipation, Long> {

    @Query("select new com.project.Teaming.domain.mentoring.dto.response.RsTeamUserDto(mp.decisionDate,u.id,u.name,mp.role,mp.participationStatus,mp.isDeleted) " +
            "from MentoringParticipation mp " +
            "join mp.user u " +
            "join mp.mentoringTeam mt " +
            "where mt = :team and (mp.participationStatus = :status or mp.participationStatus = :status2)")
    List<RsTeamUserDto> findAllByMemberStatus(@Param("team") MentoringTeam team, @Param("status") MentoringParticipationStatus status,@Param("status2") MentoringParticipationStatus status2);

    @Query("select new com.project.Teaming.domain.mentoring.dto.response.RsTeamParticipationDto(mp.requestDate,u.id,u.name,u.warningCnt,mp.participationStatus) " +
            "from MentoringParticipation mp " +
            "join mp.user u " +
            "join mp.mentoringTeam mt " +
            "where mt.id = :teamId and mp.authority <> :authority and mp.participationStatus <> :status")
    List<RsTeamParticipationDto> findAllForLeader(@Param("teamId") Long teamId, @Param("authority") MentoringAuthority authority, @Param("status") MentoringParticipationStatus status);

    @Query("select new com.project.Teaming.domain.mentoring.dto.response.RsUserParticipationDto(mp.requestDate,u.id,u.name,mp.participationStatus) " +
            "from MentoringParticipation mp " +
            "join mp.user u " +
            "join mp.mentoringTeam mt " +
            "where mt.id = :teamId and mp.authority <> :authority and mp.participationStatus <> :status")
    List<RsUserParticipationDto> findAllForUser(@Param("teamId") Long teamId, @Param("authority") MentoringAuthority authority, @Param("status") MentoringParticipationStatus status);

    @Query("select mp " +
            "from MentoringParticipation mp " +
            "join fetch mp.mentoringTeam mt " +
            "join fetch mp.user u " +
            "where mt = :mentoringTeam and u = :user and mp.authority = :authority")
    Optional<MentoringParticipation> existsByMentoringTeamAndUserAndAuthority(@Param("mentoringTeam") MentoringTeam mentoringTeam, @Param("user") User user, @Param("authority") MentoringAuthority authority);

    @Query("select mp " +
    "from MentoringParticipation mp " +
    "join fetch mp.mentoringTeam mt " +
    "join fetch mp.user u " +
    "where mt = :mentoringTeam and u = :user")
    Optional<MentoringParticipation> findByMentoringTeamAndUser(@Param("mentoringTeam") MentoringTeam mentoringTeam, @Param("user") User user);

    Optional<MentoringParticipation> findByMentoringTeamAndUserAndParticipationStatus(MentoringTeam mentoringTeam, User user, MentoringParticipationStatus status);
}
