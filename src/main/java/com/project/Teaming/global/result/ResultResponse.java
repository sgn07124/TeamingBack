package com.project.Teaming.global.result;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResultResponse<T> {

    private int status;
    private String code;
    private String message;
    private List<T> data;

    public ResultResponse(ResultCode resultCode, List<T> data) {
        this.status = resultCode.getStatus();
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
        this.data = data;
    }


}
