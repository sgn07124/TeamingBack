package com.project.Teaming.global.error.exception;

import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.ErrorResponse;

import java.util.List;

public class MentoringParticipationAlreadyExistException extends BusinessException{
    public MentoringParticipationAlreadyExistException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

    public MentoringParticipationAlreadyExistException(ErrorCode errorCode) {
        super(errorCode);
    }

    public MentoringParticipationAlreadyExistException(ErrorCode errorCode, List<ErrorResponse.FieldError> errors) {
        super(errorCode, errors);
    }
}
