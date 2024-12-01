package com.project.Teaming.domain.mentoring.repository;

import com.project.Teaming.domain.mentoring.dto.response.RsBoardDto;
import com.project.Teaming.domain.mentoring.entity.MentoringBoard;
import com.project.Teaming.domain.mentoring.entity.PostStatus;
import com.project.Teaming.domain.mentoring.entity.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MentoringBoardRepository extends JpaRepository<MentoringBoard,Long>,BoardRepositoryCustom {

}
