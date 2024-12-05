package com.project.Teaming.domain.mentoring.repository;

import com.project.Teaming.domain.mentoring.dto.response.RsTeamParticipationDto;
import com.project.Teaming.domain.mentoring.dto.response.RsTeamUserDto;
import com.project.Teaming.domain.mentoring.entity.MentoringAuthority;
import com.project.Teaming.domain.mentoring.entity.MentoringParticipation;
import com.project.Teaming.domain.mentoring.entity.MentoringParticipationStatus;
import com.project.Teaming.domain.mentoring.entity.MentoringTeam;
import com.project.Teaming.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MentoringParticipationRepository extends JpaRepository<MentoringParticipation, Long> {

    @Query("select new com.project.Teaming.domain.mentoring.dto.response.RsTeamUserDto(mp.lastModifiedDate,u.id,u.name,mp.role) " +
            "from MentoringParticipation mp " +
            "join fetch mp.user u " +
            "where mp.mentoringTeam = :team and mp.participationStatus = :status ")
    List<RsTeamUserDto> findAllByMemberStatus(@Param("team") MentoringTeam team, @Param("status") MentoringParticipationStatus status);

    @Query("select new com.project.Teaming.domain.mentoring.dto.response.RsTeamParticipationDto(mp.createdDate,u.id,u.name,u.warningCnt) " +
            "from MentoringParticipation mp " +
            "join fetch mp.user u " +
            "where mp.mentoringTeam.id = :teamId and mp.authority <> :authority ")
    List<RsTeamParticipationDto> findAllForLeader(@Param("teamId") Long teamId, @Param("authority") MentoringAuthority authority);

    @Query("select new com.project.Teaming.domain.mentoring.dto.response.RsUserParticipationDto(mp.createdDate,u.id,u.name,mp.participationStatus) " +
            "from MentoringParticipation mp " +
            "join fetch mp.user u " +
            "where mp.mentoringTeam.id = :teamId and mp.authority <> :authority ")
    List<RsTeamParticipationDto> findAllForUser(@Param("teamId") Long teamId, @Param("authority") MentoringAuthority authority);

    boolean existsByMentoringTeamAndUserAndAuthority(MentoringTeam mentoringTeam, User user, MentoringAuthority authority);

    Optional<MentoringParticipation> findByMentoringTeamAndUser(MentoringTeam mentoringTeam, User user);

    Optional<MentoringParticipation> findByMentoringTeamAndUserAndParticipationStatus(MentoringTeam mentoringTeam, User user, MentoringParticipationStatus status);
}
