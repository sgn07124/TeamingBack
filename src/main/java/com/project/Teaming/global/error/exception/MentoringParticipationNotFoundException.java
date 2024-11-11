package com.project.Teaming.global.error.exception;


import com.project.Teaming.global.error.ErrorCode;

public class MentoringParticipationNotFoundException extends BusinessException {

    public MentoringParticipationNotFoundException() {
        super(ErrorCode.MENTORING_PARTICIPATION_NOT_EXIST);
    }
}
