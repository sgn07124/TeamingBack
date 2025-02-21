package com.project.Teaming.domain.mentoring.controller;

import com.project.Teaming.domain.mentoring.dto.request.TeamRequest;
import com.project.Teaming.domain.mentoring.dto.response.TeamAuthorityResponse;
import com.project.Teaming.domain.mentoring.service.MentoringTeamService;
import com.project.Teaming.global.result.ResultCode;
import com.project.Teaming.global.result.ResultDetailResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/mentoring")
@RequiredArgsConstructor
public class MentoringTeamController implements SwaggerMentoringTeamController {

    private final MentoringTeamService mentoringTeamService;

    @Override
    @PostMapping("/teams")
    public ResultDetailResponse<String> saveMentoringTeam(@RequestBody @Valid TeamRequest dto) {
        log.info("SecurityContext Authentication: {}", SecurityContextHolder.getContext().getAuthentication());
        return new ResultDetailResponse<>(ResultCode.REGISTER_MENTORING_TEAM,
                String.valueOf( mentoringTeamService.saveMentoringTeam(dto)));
    }
    @Override
    @PutMapping("/teams/{teamId}")
    public ResultDetailResponse<TeamAuthorityResponse> updateMentoringTeam(@PathVariable Long teamId,
                                                                           @RequestBody @Valid TeamRequest dto) {
        mentoringTeamService.updateMentoringTeam(teamId, dto);

        return new ResultDetailResponse<>(ResultCode.UPDATE_MENTORING_TEAM,
                mentoringTeamService.getMentoringTeam(
                        mentoringTeamService.findMentoringTeam(teamId)));
    }
    @Override
    @GetMapping("/teams/{teamId}")
    public ResultDetailResponse<TeamAuthorityResponse> findMentoringTeam(@PathVariable Long teamId) {
        return new ResultDetailResponse<>(ResultCode.GET_MENTORING_TEAM,
                mentoringTeamService.getMentoringTeam(
                        mentoringTeamService.findMentoringTeam(teamId)));
    }
    @Override
    @DeleteMapping("/teams/{teamId}")
    public ResultDetailResponse<Void> deleteMentoringTeam(@PathVariable Long teamId) {
        mentoringTeamService.deleteMentoringTeam(teamId);  //멘토링팀 삭제처리
        return new ResultDetailResponse<>(ResultCode.DELETE_MENTORING_TEAM, null);
    }
}
