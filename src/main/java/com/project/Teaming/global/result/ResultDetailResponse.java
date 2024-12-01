package com.project.Teaming.global.result;

import com.project.Teaming.global.result.ResultCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResultDetailResponse<T> {

    private int status;
    private String code;
    private String message;
    private T data;

    public ResultDetailResponse(ResultCode resultCode, T data) {
        this.status = resultCode.getStatus();
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
        this.data = data;
    }
}
