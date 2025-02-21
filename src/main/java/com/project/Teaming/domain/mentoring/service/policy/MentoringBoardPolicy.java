package com.project.Teaming.domain.mentoring.service.policy;

import com.project.Teaming.domain.mentoring.entity.MentoringBoard;
import com.project.Teaming.domain.mentoring.entity.MentoringTeam;
import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MentoringBoardPolicy {


    public void validatePostWithTeam(MentoringBoard post, MentoringTeam mentoringTeam) {
        if (post.getMentoringTeam() != mentoringTeam) {
            throw new BusinessException(ErrorCode.NOT_A_POST_OF_TEAM);
        }
    }
}
