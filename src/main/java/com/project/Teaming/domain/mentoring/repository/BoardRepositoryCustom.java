package com.project.Teaming.domain.mentoring.repository;

import com.project.Teaming.domain.mentoring.entity.MentoringBoard;
import com.project.Teaming.domain.mentoring.entity.Status;
import java.util.List;

public interface BoardRepositoryCustom {
    List<Long> findMentoringBoardIds(Long lastCursor, int size, Status flag);

    List<MentoringBoard> findAllByIds(List<Long> ids);
}
