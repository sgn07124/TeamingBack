package com.project.Teaming.domain.mentoring.controller;

import com.project.Teaming.domain.mentoring.dto.request.MentoringReportRequest;
import com.project.Teaming.domain.mentoring.dto.request.MentoringReviewRequest;
import com.project.Teaming.domain.mentoring.dto.request.ParticipationRequest;
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
    @PostMapping("/posts/{postId}/participants")
    public ResultDetailResponse<String> saveMentoringParticipation(@PathVariable Long postId) {
        MentoringBoard mentoringPost = mentoringBoardService.findMentoringPost(postId);
        ParticipationRequest participationDto = new ParticipationRequest(MentoringAuthority.NoAuth, MentoringParticipationStatus.PENDING, mentoringPost.getRole());
        return new ResultDetailResponse<>(ResultCode.REGISTER_MENTORING_PARTICIPATION, String.valueOf(mentoringParticipationService.saveMentoringParticipation(
                mentoringPost, participationDto).getId()));
    }

    @Override
    @DeleteMapping("/teams/{teamId}/participants")
    public ResultDetailResponse<Void> cancelMentoringParticipation(@PathVariable Long teamId) {
        mentoringParticipationService.cancelMentoringParticipation(teamId);
        return new ResultDetailResponse<>(ResultCode.CANCEL_MENTORING_PARTICIPATION, null);
    }

    @Override
    @PatchMapping("/teams/{teamId}/participants/{participantId}")
    public ResultDetailResponse<Void> acceptParticipant(@PathVariable Long teamId, @PathVariable Long participantId) {
        mentoringParticipationService.acceptMentoringParticipation(teamId, participantId);
        return new ResultDetailResponse<>(ResultCode.ACCEPT_MENTORING_PARTICIPATION, null);
    }
    @Override
    @DeleteMapping("/teams/{teamId}/participants/{participantId}")
    public ResultDetailResponse<Void> rejectParticipant(@PathVariable Long teamId, @PathVariable Long participantId) {
        mentoringParticipationService.rejectMentoringParticipation(teamId, participantId);
        return new ResultDetailResponse<>(ResultCode.REJECT_MENTORING_PARTICIPATION, null);
    }
    @Override
    @DeleteMapping("/teams/{teamId}/users/{userId}")
    public ResultDetailResponse<Void> exportTeamUser(@PathVariable Long teamId, @PathVariable Long userId) {
        mentoringParticipationService.exportTeamUser(teamId, userId);
        return new ResultDetailResponse<>(ResultCode.EXPORT_TEAM_USER, null);
    }
    @Override
    @DeleteMapping("/teams/{teamId}/users")
    public ResultDetailResponse<Void> deleteParticipant(@PathVariable Long teamId) {
        mentoringParticipationService.deleteUser(teamId);
        return new ResultDetailResponse<>(ResultCode.DELETE_PARTICIPATION, null);
    }

    @Override
    @PostMapping("/reports")
    public ResultDetailResponse<Void> reportUser(@RequestBody @Valid MentoringReportRequest dto) {
        mentoringReportService.reportTeamUser(dto);
        return new ResultDetailResponse<>(ResultCode.REPORT_TEAM_USER, null);
    }


    @Override
    @PostMapping("/reviews")
    public ResultDetailResponse<Void> reviewUser(@RequestBody @Valid MentoringReviewRequest dto) {
        mentoringReviewService.review(dto);
        return new ResultDetailResponse<>(ResultCode.REVIEW_TEAM_USER, null);
    }

    @Override
    @GetMapping("/teams/{teamId}/status")
    public ResultDetailResponse<?> getParticipationAppliers(@PathVariable Long teamId) {
        return new ResultDetailResponse<>(ResultCode.GET_MEMBER_INFO, mentoringParticipationService.getParticipantsInfo(teamId));
    }
}
