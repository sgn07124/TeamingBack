package com.project.Teaming.domain.mentoring.repository;

import com.project.Teaming.domain.mentoring.dto.response.RsTeamParticipationDto;
import com.project.Teaming.domain.mentoring.dto.response.RsTeamUserDto;
import com.project.Teaming.domain.mentoring.dto.response.RsUserParticipationDto;
import com.project.Teaming.domain.mentoring.entity.*;
import com.project.Teaming.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MentoringParticipationRepository extends JpaRepository<MentoringParticipation, Long> {

    @Query("select new com.project.Teaming.domain.mentoring.dto.response.RsTeamUserDto(" +
            "mp.decisionDate, u.id, u.name, mp.role, mp.participationStatus, mp.isDeleted, " +
            "case when mt.status = :teamStatus then " +
            "     case when r.id is not null then true " +
            "          else false end " +
            "else cast(null as boolean) end) " +
            "from MentoringParticipation mp " +
            "join mp.user u " +
            "join mp.mentoringTeam mt " +
            "left join Review r " +
            "on r.reviewee.id = u.id " +
            "and r.mentoringParticipation.id = :reviewerParticipationId " +
            "where mt = :team " +
            "and (mp.participationStatus = :status or mp.participationStatus = :status2)")
    List<RsTeamUserDto> findAllByMemberStatus(
            @Param("team") MentoringTeam team,
            @Param("teamStatus") MentoringStatus teamStatus,
            @Param("status") MentoringParticipationStatus status,
            @Param("status2") MentoringParticipationStatus status2,
            @Param("reviewerParticipationId") Long reviewerParticipationId
    );

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


    Optional<MentoringParticipation> findByMentoringTeamAndUserAndAuthority(MentoringTeam mentoringTeam, User user, MentoringAuthority authority);

    @Query("select mp " +
    "from MentoringParticipation mp " +
    "join fetch mp.mentoringTeam mt " +
    "join fetch mp.user u " +
    "where mt = :mentoringTeam and u = :user")
    Optional<MentoringParticipation> findByMentoringTeamAndUser(@Param("mentoringTeam") MentoringTeam mentoringTeam, @Param("user") User user);

    @Query("SELECT mp FROM MentoringParticipation mp JOIN mp.mentoringTeam mt " +
            "WHERE mt.id = :teamId AND mp.isDeleted = false AND mp.participationStatus = :participationStatus AND mp.authority = :authority " +
            "ORDER BY mp.decisionDate ASC")
    List<MentoringParticipation> findTeamUsers(
            @Param("teamId") Long teamId,
            @Param("participationStatus") MentoringParticipationStatus participationStatus,
            @Param("authority") MentoringAuthority authority);

    @Query("select mp from " +
    "MentoringParticipation mp " +
    "join mp.mentoringTeam mt " +
    "join mp.user u " +
    "where mt = :mentoringTeam and u = :user and mp.participationStatus = :status and mp.isDeleted = false")
    Optional<MentoringParticipation> findByMentoringTeamAndUserAndParticipationStatus(
            @Param("mentoringTeam") MentoringTeam mentoringTeam,@Param("user") User user,@Param("status") MentoringParticipationStatus status);

    @Query("SELECT mp FROM MentoringParticipation mp " +
            "JOIN FETCH mp.mentoringTeam mt " +
            "WHERE mp.user = :user AND mp.participationStatus = :status AND mp.isDeleted = false AND mt.flag != :flag")
    List<MentoringParticipation> findParticipationsWithTeamsAndUser(
            @Param("user") User user,
            @Param("status") MentoringParticipationStatus status,
            @Param("flag") Status flag);

    @Query("SELECT COUNT(mp) FROM MentoringParticipation mp " +
            "WHERE mp.mentoringTeam.id = :teamId " +
            "AND mp.participationStatus = :status " +
            "AND mp.isDeleted = false")
    long countByMentoringTeamIdAndParticipationStatusAndIsDeleted(@Param("teamId") Long teamId, @Param("status") MentoringParticipationStatus status);

}
