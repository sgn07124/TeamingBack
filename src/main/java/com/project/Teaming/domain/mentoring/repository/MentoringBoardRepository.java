package com.project.Teaming.domain.mentoring.repository;

import com.project.Teaming.domain.mentoring.entity.MentoringBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentoringBoardRepository extends JpaRepository<MentoringBoard,Long> {

    Page<MentoringBoard> findAll(Pageable pageable);
}
