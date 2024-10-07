package com.project.Teaming.global.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

    // User
    REGISTER_SUCCESS(200,"U001","회원가입 완료"),
    LOGIN_SUCCESS(200, "U002", "로그인 완료"),
    REFRESH_SUCCESS(200, "U003", "재발급 완료"),
    LOGOUT_SUCCESS(200, "U004", "로그아웃 완료"),
    GET_USER_INFO_SUCCESS(200,"U005","유저 정보 조회 완료"),

    REGISTER_PORTFOLIO(200, "P001", "포트폴리오 등록 완료"),
    GET_USER_PORTFOLIO(200, "P002", "사용자 포트폴리오 조회 완료");


    private int status;
    private final String code;
    private final String message;
}
