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
    CHOOSE_ONE_DOMAIN(400,"C006","멘토링유저와 프로젝트 유저중 하나를 선택해야합니다."),

    // User
    USER_NOT_EXIST(404, "U001", "member not exist"),
    USER_EMAIL_ALREADY_EXISTS(400, "U002", "user email already exists"),
    NO_AUTHORITY(403, "U003", "권한이 없습니다."),
    NEED_LOGIN(401, "U004", "need login"),
    AUTHENTICATION_NOT_FOUND(400, "U005", "security context에 인증정보가 없습니다"),
    USER_ALREADY_LOGOUT(400, "U006", "member already logout"),

    // Auth
    REFRESH_TOKEN_INVALID(400, "A001", "refresh token invalid"),
    REFRESH_TOKEN_NOT_IN_REDIS(400, "A002", "RefreshToken이 Redis에 존재하지 않습니다."),


    // Portfolio
    PORTFOLIO_NOT_EXIST(404, "P001", "포트폴리오를 찾을 수 없습니다."),

    // ProjectTeam
    NOT_VALID_STACK_ID(400, "PT001", "유효하지 않은 스택 id가 포함되어 있습니다."),
    NOT_VALID_RECRUIT_CATEGORY_ID(400, "PT002", "유효하지 않은 모집 구분 id가 포함되어 있습니다."),
    PROJECT_NOT_COMPLETE(404, "PT003", "프로젝트의 상태가 COMPLETE가 아닙니다."),

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
    NOT_FOUND_PROJECT_POST(404, "PP011", "게시물을 찾을 수 없습니다."),
    NOT_FOUND_PROJECT_OWNER(404, "P012", "프로젝트 팀장을 찾을 수 없습니다."),
    FAIL_TO_EXPORT_TEAM(404, "P013", "해당 팀원을 내보낼 수 없습니다."),
    FAIL_TO_UPDATE_TEAM_STATUS(404, "P014", "팀 상태를 변경할 수 없습니다."),

    //Mentoring
    MENTORING_TEAM_NOT_EXIST(404,"M001","mentoring team not exist"),
    MENTORING_PARTICIPATION_NOT_EXIST(404, "M002", "mentoring participation not exist"),
    MENTORING_POST_NOT_EXIST(404, "M003", "mentoring post not exist"),
    ALREADY_MEMBER_OF_TEAM(404, "M004", "이미 해당 팀의 구성원입니다."),
    ALREADY_PARTICIPATED(404,"M005","이미 신청한 팀 입니다."),
    REJECTED_FROM_MENTORING_TEAM(404,"M006","이미 거절된 팀 입니다."),
    NOT_A_LEADER(404, "M007", "리더만 접근 가능합니다."),
    STATUS_IS_NOT_PENDING(404, "M008", "이미 수락 또는 거절된 지원자 입니다"),
    EXPORTED_BY_TEAM(404, "M009", "이미 강퇴된 팀 입니다."),
    NOT_A_MEMBER(404, "M010", "팀 구성원이 아닙니다."),
    EXPORTED_MEMBER_NOT_EXISTS(404, "M011", "강퇴할 대상을 팀에서 찾을 수 없습니다."),
    NO_ELIGIBLE_MEMBER_FOR_LEADER(404, "M012", "리더의 권한을 받을 팀원이 없습니다. 팀을 삭제해주세요."),
    NOT_A_MEMBER_OF_TEAM(404, "M013", "유저가 포함되지 않은 팀입니다."),
    NOT_A_POST_OF_TEAM(404, "M014", "해당 팀에 포함되지 않은 글입니다."),
    ASYNC_OPERATION_FAILED(404, "M015", "비동기 작업 중 오류 발생"),
    CONFLICT(404, "M016", "수정하는 동안 게시글이 삭제되었거나, 업데이트 되었습니다. 다시 확인해주세요."),

    //MentoringCategory
    NO_SUCH_CATEGORY(404, "MC001", "해당하는 카테고리가 존재하지 않습니다"),
    // Report & Review
    INVALID_REPORT_TARGET(404, "R001", "신고자를 찾을 수 없습니다."),
    INVALID_REVIEW_TARGET(404, "R002", "리뷰 대상자를 찾을 수 없습니다."),
    INVALID_SELF_ACTION(404, "R003", "자기 자신에 대해서는 불가합니다."),
    STILL_TEAM_USER(404, "R004", "아직 팀구성원이여서 신고할 수 없습니다."),
    NOT_A_TEAM_USER(404, "R005", "신고할 수 있는 권한이 없습니다."),
    ALREADY_REPORTED(404, "R006", "이미 신고한 사용자입니다."),
    ALREADY_REVIEWED(404, "R007", "이미 리뷰를 작성한 사용자입니다."),
    CANNOT_REVIEW(404, "R008", "아직 팀 상태가 완료되지 않았거나 강퇴나 탈퇴하지 않은 사용자입니다.");

    private int status;
    private final String code;
    private final String message;
}
