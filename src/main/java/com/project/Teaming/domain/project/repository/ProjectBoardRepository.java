package com.project.Teaming.domain.project.repository;

import com.project.Teaming.domain.project.entity.PostStatus;
import com.project.Teaming.domain.project.entity.ProjectBoard;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectBoardRepository extends JpaRepository<ProjectBoard, Long> {

    @Query("SELECT pb FROM ProjectBoard pb WHERE pb.projectTeam.id = :teamId ORDER BY pb.createdDate DESC ")
    List<ProjectBoard> findAllByProjectTeamId(@Param("teamId") Long teamId);

    List<ProjectBoard> findAllByStatus(PostStatus status);

    @Query("SELECT pb FROM ProjectBoard pb "
            + "JOIN FETCH pb.projectTeam pt "
            + "WHERE (:cursor IS NULL OR pb.id <= :cursor) "
            + "ORDER BY pb.createdDate DESC ")
    List<ProjectBoard> findAllByCursor(@Param("cursor") Long cursor, Pageable pageable);

    void deleteByProjectTeamId(Long teamId);
}
