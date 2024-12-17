package com.project.Teaming.domain.mentoring.repository;

import com.project.Teaming.domain.mentoring.dto.response.RsBoardDto;
import com.project.Teaming.domain.mentoring.entity.MentoringBoard;
import com.project.Teaming.domain.mentoring.entity.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MentoringBoardRepository extends JpaRepository<MentoringBoard,Long>, BoardRepositoryCustom {

    @Query("SELECT new com.project.Teaming.domain.mentoring.dto.response.RsBoardDto(pb.id, pb.title,mt.name, mt.startDate, mt.endDate, pb.contents) " +
            "FROM MentoringBoard pb " +
            "JOIN pb.mentoringTeam mt " +
            "WHERE mt.id = :teamId " +
            "ORDER BY pb.createdDate desc")
    List<RsBoardDto> findAllByMentoringTeamId(@Param("teamId") Long teamId);

    /**
     * 수정필요, n+1문제고려
     * @param teamId
     * @return
     */
    @Query("SELECT pb.id, c.id FROM MentoringBoard pb " +
            "JOIN pb.mentoringTeam mt " +
            "JOIN mt.categories tc " +
            "JOIN tc.category c " +
            "WHERE mt.id = :teamId")
    List<Object[]> findAllCategoriesByMentoringTeamId(@Param("teamId") Long teamId);

    @Modifying
    @Query("update ProjectBoard p set p.status = :newStatus where p.status = :currentStatus and p.deadline < :now")
    void bulkUpDateStatus(@Param("newStatus") PostStatus newStatus,
                          @Param("currentStatus") PostStatus currentStatus,
                          @Param("now") LocalDate now);


}
