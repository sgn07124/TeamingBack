package com.project.Teaming.global.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // Common
    INTERNAL_SERVER_ERROR(500, "C001", "internal server error"),
    INVALID_INPUT_VALUE(400, "C002", "invalid input type"),
    METHOD_NOT_ALLOWED(405, "C003", "method not allowed"),
    INVALID_TYPE_VALUE(400, "C004", "invalid type value"),
    BAD_CREDENTIALS(400, "C005", "bad credentials"),

    // User
    USER_NOT_EXIST(404, "U001", "member not exist"),
    USER_EMAIL_ALREADY_EXISTS(400, "U002", "user email already exists"),
    NO_AUTHORITY(403, "U003", "no authority"),
    NEED_LOGIN(401, "U004", "need login"),
    AUTHENTICATION_NOT_FOUND(400, "U005", "security context에 인증정보가 없습니다"),
    USER_ALREADY_LOGOUT(400, "U006", "member already logout"),

    // Auth
    REFRESH_TOKEN_INVALID(400, "A001", "refresh token invalid"),

    // Portfolio
    PORTFOLIO_NOT_EXIST(404, "P001", "포트폴리오를 찾을 수 없습니다."),

    // ProjectTeam
    NOT_VALID_STACK_ID(400, "PT001", "유효하지 않은 스택 id가 포함되어 있습니다."),
    NOT_VALID_RECRUIT_CATEGORY_ID(400, "PT002", "유효하지 않은 모집 구분 id가 포함되어 있습니다."),

    // ProjectParticipation
    NOT_FOUND_PROJECT_TEAM(404, "PP001", "해당 프로젝트 팀을 찾을 수 없습니다"),
    NOT_FOUND_USER(404, "PP002", "사용자를 찾을 수 없습니다"),
    ALREADY_PARTICIPATED_OWNER(404, "PP003", "해당 팀의 팀장입니다. 신청할 수 없습니다."),
    ALREADY_PARTICIPATED_MEMBER(404, "PP004", "해당 팀의 팀원입니다. 신청할 수 없습니다."),
    NOT_FOUND_PROJECT_PARTICIPATION(404, "PP005", "프로젝트 대기열을 찾을 수 없습니다."),
    INVALID_PARTICIPATION_ERROR(500, "PP006", "취소할 수 없습니다."),
    CANNOT_QUIT_TEAM(404, "PP007", "팀을 탈퇴할 수 없습니다."),
    CANNOT_ACCEPT_MEMBER(404, "PP008", "신청자를 수락할 수 없습니다."),
    CANNOT_REJECT_MEMBER(404, "PP009", "신청자를 거절할 수 없습니다."),
    USER_NOT_PART_OF_TEAM(404, "PP010", "사용자가 팀의 구성원이 아니거나 참여 상태가 승인되지 않았습니다."),

    //Mentoring
    MENTORING_TEAM_NOT_EXIST(404,"M001","mentoring team not exist"),
    MENTORING_PARTICIPATION_NOT_EXIST(404, "M002", "mentoring participation not exist"),
    MENTORING_POST_NOT_EXIST(404, "M003", "mentoring post not exist");

    private int status;
    private final String code;
    private final String message;
}
