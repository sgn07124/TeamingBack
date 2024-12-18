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
    UPDATE_USER_INFO(200, "U007", "유저의 닉네임, 소개, 기술 스택 수정 완료"),
    UPDATE_USER_INFO_STACK(200, "U008", "유저의 기술 스택 수정 완료"),
    REGISTER_ADDITIONAL_USER_INFO(200, "U009", "추가 정보 기입 완료"),
    GET_USER_INFO(200, "U010", "회원 정보 조회 완료"),

    // Portfolio
    REGISTER_PORTFOLIO(200, "P001", "포트폴리오 등록 완료"),
    GET_USER_PORTFOLIO(200, "P002", "사용자 포트폴리오 조회 완료"),
    UPDATE_USER_PORTFOLIO(200, "P003", "사용자 포트폴리오 수정 완료"),

    // ProjectTeam
    REGISTER_PROJECT_TEAM(200, "P001", "프로젝트 팀 생성 완료"),
    GET_PROJECT_TEAM(200, "P002", "프로젝트 팀 정보 조회 완료"),
    UPDATE_PROJECT_TEAM(200, "P003", "프로젝트 팀 정보 수정 완료"),
    DELETE_PROJECT_TEAM(200, "P004", "프로젝트 팀 삭제 완료"),
    JOIN_MEMBER_PROJECT_TEAM(200, "P005", "프로젝트에 멤버로 신청 완료"),
    CANCEL_PROJECT_TEAM(200, "P006", "프로젝트 신청 취소 완료"),
    QUIT_PROJECT_TEAM(200, "P007", "프로젝트 탈퇴 완료"),
    ACCEPT_JOIN_MEMBER(200, "P008", "팀원으로 수락 완료"),
    REJECT_JOIN_MEMBER(200, "P009", "팀원 거절 완료"),
    GET_PARTICIPATION_LIST(200, "P010", "신청자 목록 조회 완료"),
    REGISTER_PROJECT_POST(200, "P011", "프로젝트 모집 글 작성 완료"),
    GET_PROJECT_POST_INFO(200, "P012", "프로젝트 모집 글 상세 조회 완료"),
    UPDATE_PROJECT_POST_INFO(200, "P013", "프로젝트 모집 글 수정 완료"),
    DELETE_PROJECT_POST_INFO(200, "P014", "프로젝트 모집 글 삭제 완료"),
    GET_PROJECT_POST_LIST(200, "P015", "프로젝트 모집 글 조회 완료"),
    GET_PROJECT_POST_STATUS(200, "P016", "프로젝트 모집 글 상태 조회 완료"),
    EXPORT_TEAM_MEMBER(200, "P017", "프로젝트 팀원 내보내기 완료"),
    UPDATE_TEAM_STATUS(200, "P018", "프로젝트 팀 상태 변경 완료"),
    GET_MY_PROJECT(200, "P019", "참여 중인 프로젝트 조회 완료"),
    GET_MEMBER_LIST(200, "P020", "프로젝트 멤버 조회 완료"),
    REPORT_MEMBER(200, "P021", "팀원 신고 완료"),

    //Mentoring
    REGISTER_MENTORING_TEAM(200,"M001","멘토링 팀 등록 완료"),
    UPDATE_MENTORING_TEAM(200,"M002","멘토링 팀 수정 완료"),
    DELETE_MENTORING_TEAM(200, "M003", "멘토링팀 삭제 완료"),
    GET_MENTORING_TEAM(200, "M004", "멘토링팀 조회 완료"),
    GET_MY_ALL_MENTORING_TEAM(200,"M005","내 모든 멘토링팀 모두 조회완료"),
    REGISTER_MENTORING_POST(200, "M006", "멘토링 팀의 게시물 등록 완료"),
    GET_ALL_MENTORING_POSTS(200,"M007", "모든 멘토링 게시물들 조희"),
    GET_ALL_MY_MENTORING_POSTS(200,"M008", "특정 멘토링 팀의 모든 게시물들 조희"),
    GET_MENTORING_POST(200, "M009", "멘토링 게시판의 특정 게시물 조회"),
    DELETE_MENTORING_POST(200, "M010", "멘토링 글 삭제 왼료"),
    UPDATE_MENTORING_POST(200, "M011", "멘토링 글 수정 왼료"),
    REGISTER_MENTORING_PARTICIPATION(200, "M012", "멘토링 지원 등록 완료"),
    CANCEL_MENTORING_PARTICIPATION(200, "M013", "멘토링 지원 취소 완료"),
    ACCEPT_MENTORING_PARTICIPATION(200, "M014", "멘토링 지원 수락 완료"),
    REJECT_MENTORING_PARTICIPATION(200, "M015", "멘토링 지원 거절 완료"),
    GET_MEMBER_INFO(200, "M016", "멘토링 팀 멤버 및 지원자 현황 조회 완료"),
    DELETE_PARTICIPATION(200, "M017", "멘토링팀 탈퇴 완료"),
    UPDATE_POST_STATUS(200, "M18", "멘토링 글에서 모집현황 업데이트 완료"),
    EXPORT_TEAM_USER(200, "M19", "멘토링 팀원 강퇴 완료");

    private int status;
    private final String code;
    private final String message;
}
