package com.project.Teaming.domain.mentoring.provider;

import com.project.Teaming.domain.mentoring.entity.MentoringBoard;
import com.project.Teaming.domain.mentoring.repository.MentoringBoardRepository;
import com.project.Teaming.global.error.exception.MentoringPostNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MentoringBoardDataProvider {

    private final MentoringBoardRepository mentoringBoardRepository;

    public MentoringBoard findBoard(Long boardId) {
        return mentoringBoardRepository.findById(boardId)
                .orElseThrow(MentoringPostNotFoundException::new);
    }
}
