package com.project.Teaming.domain.mentoring.repository;

import com.project.Teaming.domain.mentoring.dto.response.RsBoardDto;
import com.project.Teaming.domain.mentoring.entity.MentoringStatus;
import com.project.Teaming.domain.mentoring.entity.PostStatus;
import com.project.Teaming.domain.mentoring.entity.Status;
import com.querydsl.core.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BoardRepositoryCustom {
    List<Long> findMentoringBoardIds(MentoringStatus status, Status flag, Pageable pageable);

    List<Tuple> findAllByIds(List<Long> ids);
    long countAllByStatus(MentoringStatus status, Status flag);
}
