package com.project.Teaming.domain.mentoring.repository;

import com.project.Teaming.domain.mentoring.dto.response.RsBoardDto;
import com.project.Teaming.domain.mentoring.entity.MentoringBoard;
import com.project.Teaming.domain.mentoring.entity.PostStatus;
import com.project.Teaming.domain.mentoring.entity.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MentoringBoardRepository extends JpaRepository<MentoringBoard,Long>,BoardRepositoryCustom {

    @Query("SELECT new com.project.Teaming.domain.mentoring.dto.response.RsBoardDto(pb.id, pb.title,mt.name, mt.startDate, mt.endDate, pb.contents) " +
            "FROM MentoringBoard pb " +
            "JOIN pb.mentoringTeam mt " +
            "WHERE mt.id = :teamId " +
            "ORDER BY pb.createdDate desc")
    List<RsBoardDto> findAllByMentoringTeamId(@Param("teamId") Long teamId);

    @Query("SELECT pb.id, c.id FROM MentoringBoard pb " +
            "JOIN pb.mentoringTeam mt " +
            "JOIN mt.categories tc " +
            "JOIN tc.category c " +
            "WHERE mt.id = :teamId")
    List<Object[]> findAllCategoriesByMentoringTeamId(@Param("teamId") Long teamId);
}
