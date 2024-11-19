package com.project.Teaming.domain.mentoring.repository;

import com.project.Teaming.domain.mentoring.entity.MentoringParticipation;
import com.project.Teaming.domain.mentoring.entity.MentoringParticipationStatus;
import com.project.Teaming.domain.mentoring.entity.MentoringTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MentoringParticipationRepository extends JpaRepository<MentoringParticipation, Long> {

    @Query("select i from MentoringParticipation i where i.mentoringTeam = :team and i.participationStatus = :status ")
    List<MentoringParticipation> findAllByMemberStatus(@Param("team") MentoringTeam team, @Param("status") MentoringParticipationStatus status);

}
