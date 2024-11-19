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

    //Mentoring
    MENTORING_TEAM_NOT_EXIST(404,"M001","mentoring team not exist"),
    MENTORING_PARTICIPATION_NOT_EXIST(404, "M002", "mentoring participation not exist"),
    MENTORING_POST_NOT_EXIST(404, "M003", "mentoring post not exist");

    private int status;
    private final String code;
    private final String message;
}
