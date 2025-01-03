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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/mentoring")
@RequiredArgsConstructor
public class MentoringParticipationController implements SwaggerMentoringParticipationController {

    private final MentoringParticipationService mentoringParticipationService;
    private final MentoringBoardService mentoringBoardService;
    private final MentoringReportService mentoringReportService;
    private final MentoringReviewService mentoringReviewService;

    @Override
    @PostMapping("/posts/{post_id}/participants")
    public ResultDetailResponse<String> saveMentoringParticipation(@PathVariable Long post_id) {
        MentoringBoard mentoringPost = mentoringBoardService.findMentoringPost(post_id);
        MentoringTeam mentoringTeam = mentoringPost.getMentoringTeam();
        RqParticipationDto participationDto = new RqParticipationDto(MentoringAuthority.NoAuth, MentoringParticipationStatus.PENDING, mentoringPost.getRole());
        return new ResultDetailResponse<>(ResultCode.REGISTER_MENTORING_PARTICIPATION,
                String.valueOf(mentoringParticipationService.saveMentoringParticipation(
                        mentoringTeam, participationDto).getId()));
    }

    @Override
    @DeleteMapping("/teams/{team_id}/participants")
    public ResultDetailResponse<Void> cancelMentoringParticipation(@PathVariable Long team_id) {
        mentoringParticipationService.cancelMentoringParticipation(team_id);
        return new ResultDetailResponse<>(ResultCode.CANCEL_MENTORING_PARTICIPATION, null);
    }

    @Override
    @PatchMapping("/teams/{team_id}/participants/{participant_id}/accept")
    public ResultDetailResponse<Void> acceptParticipant(@PathVariable Long team_id, @PathVariable Long participant_id) {
        mentoringParticipationService.acceptMentoringParticipation(team_id, participant_id);
        return new ResultDetailResponse<>(ResultCode.ACCEPT_MENTORING_PARTICIPATION, null);
    }
    @Override
    @PatchMapping("/teams/{team_id}/participants/{participant_id}/reject")
    public ResultDetailResponse<Void> rejectParticipant(@PathVariable Long team_id, @PathVariable Long participant_id) {
        mentoringParticipationService.rejectMentoringParticipation(team_id, participant_id);
        return new ResultDetailResponse<>(ResultCode.REJECT_MENTORING_PARTICIPATION, null);
    }
    @Override
    @PatchMapping("/teams/{team_id}/users/{user_id}/export")
    public ResultDetailResponse<Void> exportTeamUser(@PathVariable Long team_id, @PathVariable Long user_id) {
        mentoringParticipationService.exportTeamUser(team_id, user_id);
        return new ResultDetailResponse<>(ResultCode.EXPORT_TEAM_USER, null);
    }
    @Override
    @PatchMapping("/teams/{team_id}/quit")
    public ResultDetailResponse<Void> deleteParticipant(@PathVariable Long team_id) {
        mentoringParticipationService.deleteUser(team_id);
        return new ResultDetailResponse<>(ResultCode.DELETE_PARTICIPATION, null);
    }

    @Override
    @PostMapping("/report")
    public ResultDetailResponse<Void> reportUser(@RequestBody @Valid MentoringReportDto dto) {
        mentoringReportService.reportTeamUser(dto);
        return new ResultDetailResponse<>(ResultCode.REPORT_TEAM_USER, null);
    }

    @Override
    @PostMapping("/review")
    public ResultDetailResponse<Void> reviewUser(@RequestBody @Valid MentoringReviewDto dto) {
        mentoringReviewService.review(dto);
        return new ResultDetailResponse<>(ResultCode.REVIEW_TEAM_USER, null);
    }

    @Override
    @GetMapping("/teams/{team_id}/status")
    public ResultDetailResponse<?> getParticipationAppliers(@PathVariable Long team_id) {
        return new ResultDetailResponse<>(ResultCode.GET_MEMBER_INFO, mentoringParticipationService.getParticipantsInfo(team_id));
    }

}
