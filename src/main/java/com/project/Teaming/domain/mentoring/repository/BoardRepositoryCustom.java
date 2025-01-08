package com.project.Teaming.domain.mentoring.repository;

import com.project.Teaming.domain.mentoring.entity.MentoringBoard;
import com.project.Teaming.domain.mentoring.entity.PostStatus;
import com.project.Teaming.domain.mentoring.entity.Status;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BoardRepositoryCustom {
    List<Long> findMentoringBoardIds(Long lastCursor, int size, Status flag);

    List<MentoringBoard> findAllByIds(List<Long> ids);
    void deleteByTeamId(Long teamId);
    void bulkUpDateStatus(PostStatus newStatus, PostStatus currentStatus, LocalDate now);
}
