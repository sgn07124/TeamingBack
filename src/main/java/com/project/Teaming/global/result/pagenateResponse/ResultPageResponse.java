package com.project.Teaming.global.result.pagenateResponse;

import com.project.Teaming.global.result.ResultCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResultPageResponse<T> {

    private int status;
    private String code;
    private String message;
    private T data;

    public ResultPageResponse(ResultCode resultCode, T data) {
        this.status = resultCode.getStatus();
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
        this.data = data;
    }
}
