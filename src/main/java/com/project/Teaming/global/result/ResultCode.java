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
    GET_USER_WARNING_CNT(200,"U006","유저의 경고누적횟수 조회완료"),
    UPDATE_USER_NICKNAME(200, "U007", "유저의 닉네임 정보 업데이트 완료"),
    REGISTER_ADDITIONAL_USER_INFO(200, "U008", "추가 정보 기입 완료"),
    GET_USER_INFO(200, "U009", "회원 정보 조회 완료"),

    // Portfolio
    REGISTER_PORTFOLIO(200, "P001", "포트폴리오 등록 완료"),
    GET_USER_PORTFOLIO(200, "P002", "사용자 포트폴리오 조회 완료"),
    UPDATE_USER_PORTFOLIO(200, "P003", "사용자 포트폴리오 수정 완료"),

    // ProjectTeam
    REGISTER_PROJECT_TEAM(200, "PT001", "프로젝트 팀 생성 완료"),
    GET_PROJECT_TEAM(200, "PT002", "프로젝트 팀 정보 조회 완료"),
    UPDATE_PROJECT_TEAM(200, "PT003", "프로젝트 팀 정보 수정 완료"),
    DELETE_PROJECT_TEAM(200, "PT004", "프로젝트 팀 삭제 완료"),

    // ProjectParticipate
    JOIN_MEMBER_PROJECT_TEAM(200, "PP001", "프로젝트에 멤버로 신청 완료");

    private int status;
    private final String code;
    private final String message;
}
