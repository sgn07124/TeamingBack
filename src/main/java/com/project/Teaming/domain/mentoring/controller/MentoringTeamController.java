package com.project.Teaming.domain.mentoring.controller;

import com.project.Teaming.domain.mentoring.dto.request.RqTeamDto;
import com.project.Teaming.domain.mentoring.dto.response.TeamResponseDto;
import com.project.Teaming.domain.mentoring.entity.MentoringTeam;
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
    public ResultDetailResponse<String> saveMentoringTeam(@RequestBody @Valid RqTeamDto dto) {
        log.info("SecurityContext Authentication: {}", SecurityContextHolder.getContext().getAuthentication());
        return new ResultDetailResponse<>(ResultCode.REGISTER_MENTORING_TEAM,
                String.valueOf( mentoringTeamService.saveMentoringTeam(dto)));
    }
    @Override
    @PutMapping("/teams/{team_id}")
    public ResultDetailResponse<TeamResponseDto> updateMentoringTeam(@PathVariable Long team_id,
                                                                   @RequestBody @Valid RqTeamDto dto) {
        mentoringTeamService.updateMentoringTeam(team_id, dto);

        return new ResultDetailResponse<>(ResultCode.UPDATE_MENTORING_TEAM,
                mentoringTeamService.getMentoringTeam(
                        mentoringTeamService.findMentoringTeam(team_id)));
    }
    @Override
    @GetMapping("/teams/{team_id}")
    public ResultDetailResponse<TeamResponseDto> findMentoringTeam(@PathVariable Long team_id) {
        return new ResultDetailResponse<>(ResultCode.GET_MENTORING_TEAM,
                mentoringTeamService.getMentoringTeam(
                        mentoringTeamService.findMentoringTeam(team_id)));
    }
    @Override
    @DeleteMapping("/teams/{team_id}")
    public ResultDetailResponse<Void> deleteMentoringTeam(@PathVariable Long team_id) {
        mentoringTeamService.deleteMentoringTeam(team_id);  //멘토링팀 삭제처리
        return new ResultDetailResponse<>(ResultCode.DELETE_MENTORING_TEAM, null);
    }
}
