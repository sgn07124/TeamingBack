package com.project.Teaming.domain.mentoring.controller;

import com.project.Teaming.domain.mentoring.dto.request.MentoringReportDto;
import com.project.Teaming.domain.mentoring.dto.request.MentoringReviewDto;
import com.project.Teaming.domain.mentoring.dto.request.RqParticipationDto;
import com.project.Teaming.domain.mentoring.dto.response.*;
import com.project.Teaming.domain.mentoring.entity.*;
import com.project.Teaming.domain.mentoring.service.MentoringBoardService;
import com.project.Teaming.domain.mentoring.service.MentoringParticipationService;
import com.project.Teaming.domain.mentoring.service.MentoringReportService;
import com.project.Teaming.domain.mentoring.service.MentoringReviewService;
import com.project.Teaming.global.result.ResultCode;
import com.project.Teaming.global.result.ResultDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/mentoring")
@Tag(name = "MentoringParticipation", description = "멘토링 지원 관련 API")
public class MentoringParticipationController {

    private final MentoringParticipationService mentoringParticipationService;
    private final MentoringBoardService mentoringBoardService;
    private final MentoringReportService mentoringReportService;
    private final MentoringReviewService mentoringReviewService;

    @PostMapping("/posts/{post_id}/participants")
    @Operation(summary = "멘토링 지원자 등록", description = "멘토링 팀에 지원하는 API , 지원자 ID 반환")
    public ResultDetailResponse<String> saveMentoringParticipation(@PathVariable Long post_id) {
        MentoringBoard mentoringPost = mentoringBoardService.findMentoringPost(post_id);
        MentoringTeam mentoringTeam = mentoringPost.getMentoringTeam();
        RqParticipationDto participationDto = new RqParticipationDto(MentoringAuthority.NoAuth, MentoringParticipationStatus.PENDING, mentoringPost.getRole());
        Long id = mentoringParticipationService.saveMentoringParticipation(mentoringTeam, participationDto);
        return new ResultDetailResponse<>(ResultCode.REGISTER_MENTORING_PARTICIPATION, String.valueOf(id));
    }

    @DeleteMapping("/teams/{team_id}/participants")
    @Operation(summary = "멘토링 지원 취소" , description = "멘토링 팀 지원취소 하는 API")
    public ResultDetailResponse<Void> cancelMentoringParticipation(@PathVariable Long team_id) {
        mentoringParticipationService.cancelMentoringParticipation(team_id);
        return new ResultDetailResponse<>(ResultCode.CANCEL_MENTORING_PARTICIPATION, null);
    }

    @PatchMapping("/teams/{team_id}/participants/{participant_id}/accept")
    @Operation(summary = "리더의 멘토링 지원 수락" , description = "멘토링 팀 리더가 지원을 수락 하는 API")
    public ResultDetailResponse<Void> acceptParticipant(@PathVariable Long team_id, @PathVariable Long participant_id) {
        mentoringParticipationService.acceptMentoringParticipation(team_id, participant_id);
        return new ResultDetailResponse<>(ResultCode.ACCEPT_MENTORING_PARTICIPATION, null);
    }

    @PatchMapping("/teams/{team_id}/participants/{participant_id}/reject")
    @Operation(summary = "리더의 멘토링 지원 거절" , description = "멘토링 팀 리더가 지원을 거절 하는 API")
    public ResultDetailResponse<Void> rejectParticipant(@PathVariable Long team_id, @PathVariable Long participant_id) {
        mentoringParticipationService.rejectMentoringParticipation(team_id, participant_id);
        return new ResultDetailResponse<>(ResultCode.REJECT_MENTORING_PARTICIPATION, null);
    }

    @PatchMapping("/teams/{team_id}/users/{user_id}/export")
    @Operation(summary = "리더의 멘토링 팀원 강퇴" , description = "멘토링 팀 리더가 팀원을 강퇴하는 API")
    public ResultDetailResponse<Void> exportTeamUser(@PathVariable Long team_id, @PathVariable Long user_id) {
        mentoringParticipationService.exportTeamUser(team_id, user_id);
        return new ResultDetailResponse<>(ResultCode.EXPORT_TEAM_USER, null);
    }


    @PatchMapping("/teams/{team_id}/quit")
    @Operation(summary = "팀 구성원의 탈퇴", description = "팀 구성원들이 탈퇴하는 API")
    public ResultDetailResponse<Void> deleteParticipant(@PathVariable Long team_id) {
        mentoringParticipationService.deleteUser(team_id);
        return new ResultDetailResponse<>(ResultCode.DELETE_PARTICIPATION, null);
    }

    @PostMapping("/report")
    @Operation(summary = "팀 내 구성원 신고하기", description = "팀 내 구성원을 신고하는 API")
    public ResultDetailResponse<Void> reportUser(@RequestBody @Valid MentoringReportDto dto) {
        mentoringReportService.reportTeamUser(dto);
        return new ResultDetailResponse<>(ResultCode.REPORT_TEAM_USER, null);
    }

    @PostMapping("/review")
    @Operation(summary = "팀 내 구성원에게 리뷰쓰기", description = "팀 내 구성원에게 리뷰를 쓰는 API")
    public ResultDetailResponse<Void> reviewUser(@RequestBody @Valid MentoringReviewDto dto) {
        mentoringReviewService.review(dto);
        return new ResultDetailResponse<>(ResultCode.REVIEW_TEAM_USER, null);
    }

    @GetMapping("/mentoring/teams/{team_id}/status")
    @Operation(summary = "멘토링팀 멤버 및 지원자 현황 조회", description = "멘토링 팀 멤버나 지원자 현황을 조회하는 API " +
            "조회하는 사람이 팀장이면 팀원과 지원자 정보 반환, 팀원이면 팀원 정보만 반환. " +
            "리더용, 팀원용 페이지에서 팀원 조회 시 isDeleted는 탈퇴 유무, isLogined는 현재 로그인 된 사용자 유무, status가 EXPORT면 강퇴된 사용자 입니다, " +
            "리더용 페이지 지원자 현황 조회 시 PENDING은 대기중 상태, ACCEPT는 수락, REJECT는 거절, " +
            "Authority를 같이 반환하니 이거에 따라 다른 페이지 보여주세요. 팀구성원이 아닌 사용자는 팀페이지 조회에서 반환")
    public ResultDetailResponse<?> getParticipationAppliers(@PathVariable Long team_id) {
        ParticipantsDto<?> participantsInfo = mentoringParticipationService.getParticipantsInfo(team_id);
        return new ResultDetailResponse<>(ResultCode.GET_MEMBER_INFO, participantsInfo);
    }

}
