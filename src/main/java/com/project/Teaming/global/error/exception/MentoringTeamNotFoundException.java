package com.project.Teaming.global.error.exception;

import com.project.Teaming.global.error.ErrorCode;


public class MentoringTeamNotFoundException extends BusinessException{

    public MentoringTeamNotFoundException() {
        super(ErrorCode.MENTORING_TEAM_NOT_EXIST);
    }
}
