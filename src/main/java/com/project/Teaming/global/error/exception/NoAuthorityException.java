package com.project.Teaming.global.error.exception;

import com.project.Teaming.global.error.ErrorCode;

public class NoAuthorityException extends BusinessException {

    public NoAuthorityException(){
        super(ErrorCode.NO_AUTHORITY);
    }
    public NoAuthorityException(String message){
        super(message,ErrorCode.NO_AUTHORITY);
    }
}
