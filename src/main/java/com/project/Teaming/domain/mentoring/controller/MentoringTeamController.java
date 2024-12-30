package com.project.Teaming.domain.mentoring.controller;

import com.project.Teaming.domain.mentoring.dto.request.RqTeamDto;
import com.project.Teaming.domain.mentoring.dto.response.MyTeamDto;
import com.project.Teaming.domain.mentoring.dto.response.TeamResponseDto;
import com.project.Teaming.domain.mentoring.entity.MentoringTeam;
import com.project.Teaming.domain.mentoring.service.MentoringTeamService;
import com.project.Teaming.global.result.ResultCode;
import com.project.Teaming.global.result.ResultDetailResponse;
import com.project.Teaming.global.result.ResultListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/mentoring")
@Tag(name = "MentoringTeam", description = "멘토링 팀 관련 API")
public class MentoringTeamController {

    private final MentoringTeamService mentoringTeamService;

    @PostMapping("/teams")
    @Operation(summary = "멘토링 팀 저장", description = "멘토링 팀을 생성하고 저장할 수 있으며, 멘토링 팀을 생성한 유저는 팀의 리더가 된다,  " +
            "status는 RECRUITING(모집중), WORKING(진행중), COMPLETE(완료) / role은 MENTOR(멘토), MENTEE(멘티)로 요청 주시면 됩니다,  " +
            "저장된 팀의 id 반환")
    public ResultDetailResponse<String> saveMentoringTeam(@RequestBody @Valid RqTeamDto dto) {
        log.info("SecurityContext Authentication: {}", SecurityContextHolder.getContext().getAuthentication());
        Long savedId = mentoringTeamService.saveMentoringTeam(dto);
        return new ResultDetailResponse<>(ResultCode.REGISTER_MENTORING_TEAM, String.valueOf(savedId));
    }

    @PutMapping("/teams/{team_id}")
    @Operation(summary = "멘토링 팀 수정", description = "멘토링 팀을 수정할 수 있다.")
    public ResultDetailResponse<TeamResponseDto> updateMentoringTeam(@PathVariable Long team_id,
                                                                   @RequestBody @Valid RqTeamDto dto) {
        mentoringTeamService.updateMentoringTeam(team_id, dto);
        MentoringTeam mentoringTeam = mentoringTeamService.findMentoringTeam(team_id);
        TeamResponseDto teamDto = mentoringTeamService.getMentoringTeam(mentoringTeam);

        return new ResultDetailResponse<>(ResultCode.UPDATE_MENTORING_TEAM, teamDto);
    }

    @GetMapping("/teams")
    @Operation(summary = "나의 모든 멘토링 팀 조회", description = "나의 모든 멘토링 팀을 조회할 수 있다. 마이페이지에서 사용")
    public ResultListResponse<MyTeamDto> findMyMentoringTeams() {
        List<MentoringTeam> myMentoringTeams = mentoringTeamService.findMyMentoringTeams();
        List<MyTeamDto> teams = myMentoringTeams.stream()
                .map(mentoringTeamService::getMyTeam)
                .collect(Collectors.toList());
        return new ResultListResponse<>(ResultCode.GET_MY_ALL_MENTORING_TEAM, teams);
    }

    @GetMapping("/teams/{team_id}")
    @Operation(summary = "멘토링 팀 조회", description = "특정 멘토링 팀을 조회할 수 있다. " +
            "멘토링 팀페이지 조회용, authority가 LEADER면 팀장용페이지,CREW이면 팀원용페이지, NoAuth면 일반사용자용 페이지 띄워주세요 ")
    public ResultDetailResponse<TeamResponseDto> findMentoringTeam(@PathVariable Long team_id) {
        MentoringTeam mentoringTeam = mentoringTeamService.findMentoringTeam(team_id);
        TeamResponseDto teamDto = mentoringTeamService.getMentoringTeam(mentoringTeam);
        return new ResultDetailResponse<>(ResultCode.GET_MENTORING_TEAM, teamDto);
    }

    @DeleteMapping("/mentoring/teams/{team_id}")
    @Operation(summary = "멘토링 팀 삭제", description = "특정 멘토링 팀을 삭제할 수 있다.")
    public ResultDetailResponse<Void> deleteMentoringTeam(@PathVariable Long team_id) {
        mentoringTeamService.deleteMentoringTeam(team_id);  //멘토링팀 삭제처리
        return new ResultDetailResponse<>(ResultCode.DELETE_MENTORING_TEAM, null);
    }
}
