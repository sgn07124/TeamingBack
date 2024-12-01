package com.project.Teaming.domain.project.repository;

import com.project.Teaming.domain.project.entity.PostStatus;
import com.project.Teaming.domain.project.entity.ProjectBoard;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectBoardRepository extends JpaRepository<ProjectBoard, Long> {

    @Query("SELECT pb FROM ProjectBoard  pb "
            + "JOIN FETCH pb.projectTeam pt "
            + "WHERE (:status IS NULL OR pb.status = :status) "
            + "ORDER BY pb.createdDate DESC ")
    Page<ProjectBoard> findAllByStatusOptional(@Param("status")PostStatus status, Pageable pageable);

    @Query("SELECT pb FROM ProjectBoard pb WHERE pb.projectTeam.id = :teamId ORDER BY pb.createdDate DESC ")
    List<ProjectBoard> findAllByProjectTeamId(@Param("teamId") Long teamId);
}
