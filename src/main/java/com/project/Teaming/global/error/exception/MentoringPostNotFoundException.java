package com.project.Teaming.global.error.exception;

import com.project.Teaming.global.error.ErrorCode;

public class MentoringPostNotFoundException extends BusinessException {

    public MentoringPostNotFoundException() {
        super(ErrorCode.MENTORING_POST_NOT_EXIST);
    }

    public MentoringPostNotFoundException(String message) {
        super(message, ErrorCode.MENTORING_POST_NOT_EXIST);
    }
}
