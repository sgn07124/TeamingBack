package com.project.Teaming.domain.mentoring.repository;

import com.project.Teaming.domain.mentoring.dto.response.BoardResponse;
import com.project.Teaming.domain.mentoring.entity.MentoringBoard;
import com.project.Teaming.domain.mentoring.entity.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MentoringBoardRepository extends JpaRepository<MentoringBoard,Long>, BoardRepositoryCustom {

    @Query("SELECT new com.project.Teaming.domain.mentoring.dto.response.BoardResponse(mb.id, mb.title,mt.name, mt.startDate, mt.endDate, mb.contents, mb.status) " +
            "FROM MentoringBoard mb " +
            "JOIN mb.mentoringTeam mt " +
            "WHERE mt.id = :teamId " +
            "ORDER BY mb.createdDate desc")
    List<BoardResponse> findAllByMentoringTeamId(@Param("teamId") Long teamId);


}
