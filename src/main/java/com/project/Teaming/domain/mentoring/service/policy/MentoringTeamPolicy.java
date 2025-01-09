package com.project.Teaming.domain.mentoring.service.policy;

import com.project.Teaming.domain.mentoring.entity.MentoringTeam;
import com.project.Teaming.domain.mentoring.entity.Status;
import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MentoringTeamPolicy {

    public void validateTeamStatus(MentoringTeam mentoringTeam) {
        if (mentoringTeam.getFlag() == Status.TRUE) {
            throw new BusinessException(ErrorCode.MENTORING_TEAM_NOT_EXIST);
        }
    }
}
