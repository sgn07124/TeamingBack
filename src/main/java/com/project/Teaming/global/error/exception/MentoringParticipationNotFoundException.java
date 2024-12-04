package com.project.Teaming.global.error.exception;


import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.ErrorResponse;

import java.util.List;

public class MentoringParticipationNotFoundException extends BusinessException {

    public MentoringParticipationNotFoundException() {
        super(ErrorCode.MENTORING_PARTICIPATION_NOT_EXIST);
    }

    public MentoringParticipationNotFoundException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

    public MentoringParticipationNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public MentoringParticipationNotFoundException(ErrorCode errorCode, List<ErrorResponse.FieldError> errors) {
        super(errorCode, errors);
    }
}
