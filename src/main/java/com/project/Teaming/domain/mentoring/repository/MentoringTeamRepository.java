package com.project.Teaming.domain.mentoring.repository;

import com.project.Teaming.domain.mentoring.entity.MentoringTeam;
import com.project.Teaming.domain.mentoring.entity.Status;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MentoringTeamRepository extends JpaRepository<MentoringTeam,Long> {

    @Query("SELECT t FROM MentoringTeam t JOIN FETCH t.mentoringBoardList WHERE t.id = :teamId")
    Optional<MentoringTeam> findWithBoardsById(@Param("teamId") Long teamId);

}
