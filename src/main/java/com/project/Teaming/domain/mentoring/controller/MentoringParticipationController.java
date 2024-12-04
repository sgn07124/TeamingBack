package com.project.Teaming.domain.mentoring.controller;

import com.project.Teaming.domain.mentoring.entity.MentoringBoard;
import com.project.Teaming.domain.mentoring.entity.MentoringParticipation;
import com.project.Teaming.domain.mentoring.entity.MentoringParticipationStatus;
import com.project.Teaming.domain.mentoring.entity.MentoringTeam;
import com.project.Teaming.domain.mentoring.service.MentoringBoardService;
import com.project.Teaming.domain.mentoring.service.MentoringParticipationService;
import com.project.Teaming.domain.mentoring.service.MentoringTeamService;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.service.UserService;
import com.project.Teaming.global.jwt.dto.SecurityUserDto;
import com.project.Teaming.global.result.ResultCode;
import com.project.Teaming.global.result.ResultDetailResponse;
import com.project.Teaming.global.result.ResultListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/mentoring")
@Tag(name = "MentoringParticipation", description = "멘토링 지원 관련 API")
public class MentoringParticipationController {

    private final MentoringParticipationService mentoringParticipationService;
    private final MentoringBoardService mentoringBoardService;
    private final UserService userService;

    @PostMapping("/{post_id}/join")
    @Operation(summary = "멘토링 지원자 등록", description = "멘토링 팀에 지원하는 API")
    public ResultDetailResponse<Void> saveMentoringParticipation(@PathVariable Long post_id) {
        User user = getUser();
        MentoringBoard mentoringPost = mentoringBoardService.findMentoringPost(post_id);
        MentoringTeam mentoringTeam = mentoringPost.getMentoringTeam();
        mentoringParticipationService.saveMentoringParticipation(user.getId(), mentoringTeam.getId(), mentoringPost.getRole());
        return new ResultDetailResponse<>(ResultCode.REGISTER_MENTORING_PARTICIPATION, null);
    }

    @PostMapping("/{team_id}/cancel")
    @Operation(summary = "멘토링 지원 취소" , description = "멘토링 팀 지원취소 하는 API")
    public ResultDetailResponse<Void> cancelMentoringParticipation(@PathVariable Long team_id) {
        User user = getUser();
        mentoringParticipationService.cancelMentoringParticipation(user.getId(), team_id);
        return new ResultDetailResponse<>(ResultCode.CANCEL_MENTORING_PARTICIPATION, null);
    }

    @PostMapping("/team/{team_id}/{participant_id}/accept")
    @Operation(summary = "리더의 멘토링 지원 수락" , description = "멘토링 팀 리더가 지원을 수락 하는 API")
    public ResultDetailResponse<Void> acceptParticipant(@PathVariable Long team_id, @PathVariable Long participant_id) {
        User user = getUser();
        mentoringParticipationService.acceptMentoringParticipation(user.getId(), team_id, participant_id);
        return new ResultDetailResponse<>(ResultCode.ACCEPT_MENTORING_PARTICIPATION, null);
    }

    @PostMapping("/team/{team_id}/{participant_id}/reject")
    @Operation(summary = "리더의 멘토링 지원 거절" , description = "멘토링 팀 리더가 지원을 거절 하는 API")
    public ResultDetailResponse<Void> rejectParticipant(@PathVariable Long team_id, @PathVariable Long participant_id) {
        User user = getUser();
        mentoringParticipationService.rejectMentoringParticipation(user.getId(), team_id, participant_id);
        return new ResultDetailResponse<>(ResultCode.REJECT_MENTORING_PARTICIPATION, null);
    }


    private User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUserDto securityUser = (SecurityUserDto) authentication.getPrincipal();
        Long userId = securityUser.getUserId();
        User user = userService.findById(userId).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        return user;
    }
}
