package com.project.Teaming.global.error.exception;

import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.ErrorResponse;

import java.util.List;

public class NoAuthorityException extends BusinessException {

    public NoAuthorityException(){
        super(ErrorCode.NO_AUTHORITY);
    }
    public NoAuthorityException(String message){
        super(message,ErrorCode.NO_AUTHORITY);
    }

    public NoAuthorityException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

    public NoAuthorityException(ErrorCode errorCode) {
        super(errorCode);
    }

    public NoAuthorityException(ErrorCode errorCode, List<ErrorResponse.FieldError> errors) {
        super(errorCode, errors);
    }
}
