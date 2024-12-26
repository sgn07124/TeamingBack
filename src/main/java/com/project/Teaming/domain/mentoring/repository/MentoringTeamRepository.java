package com.project.Teaming.domain.mentoring.repository;

import com.project.Teaming.domain.mentoring.entity.MentoringStatus;
import com.project.Teaming.domain.mentoring.entity.MentoringTeam;
import com.project.Teaming.domain.mentoring.entity.Status;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MentoringTeamRepository extends JpaRepository<MentoringTeam,Long> {

    @Query("SELECT t FROM MentoringTeam t JOIN FETCH t.mentoringBoardList WHERE t.id = :teamId")
    Optional<MentoringTeam> findWithBoardsById(@Param("teamId") Long teamId);

    @Modifying
    @Query("UPDATE MentoringTeam m SET m.status = :workingStatus WHERE m.status = :recruitingStatus AND m.startDate <= CURRENT_DATE AND m.endDate > CURRENT_DATE")
    int updateStatusToWorking(@Param("workingStatus") MentoringStatus workingStatus, @Param("recruitingStatus") MentoringStatus recruitingStatus);

    @Modifying
    @Query("UPDATE MentoringTeam m SET m.status = :completeStatus WHERE m.status = :workingStatus AND m.endDate < CURRENT_DATE")
    int updateStatusToComplete(@Param("completeStatus") MentoringStatus completeStatus, @Param("workingStatus") MentoringStatus workingStatus);

}
