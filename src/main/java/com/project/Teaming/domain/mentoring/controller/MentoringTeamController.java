package com.project.Teaming.domain.mentoring.controller;

import com.project.Teaming.domain.mentoring.dto.request.RqTeamDto;
import com.project.Teaming.domain.mentoring.dto.response.MyTeamDto;
import com.project.Teaming.domain.mentoring.dto.response.TeamResponseDto;
import com.project.Teaming.domain.mentoring.entity.MentoringBoard;
import com.project.Teaming.domain.mentoring.entity.MentoringTeam;
import com.project.Teaming.domain.mentoring.service.MentoringTeamService;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.service.UserService;
import com.project.Teaming.global.jwt.dto.SecurityUserDto;
import com.project.Teaming.global.result.ResultCode;
import com.project.Teaming.global.result.ResultResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
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
    private final UserService userService;

    @PostMapping("/team")
    @Operation(summary = "멘토링 팀 저장", description = "멘토링 팀을 생성하고 저장할 수 있으며, 멘토링 팀을 생성한 유저는 팀의 리더가 된다.")
    public ResultResponse<Long> saveMentoringTeam(@RequestBody @Valid RqTeamDto dto) {
        Long id = getUserId();
        log.info("SecurityContext Authentication: {}", SecurityContextHolder.getContext().getAuthentication());
        mentoringTeamService.saveMentoringTeam(id, dto);
        return new ResultResponse<>(ResultCode.REGISTER_MENTORING_TEAM, null);
    }

    @PostMapping("/team/{teamId}")
    @Operation(summary = "멘토링 팀 수정", description = "멘토링 팀을 수정할 수 있다.")
    public ResultResponse<TeamResponseDto> updateMentoringTeam(@PathVariable Long teamId,
                                                               @RequestBody @Valid RqTeamDto dto) {
        Long userId = getUserId();
        mentoringTeamService.updateMentoringTeam(userId,teamId, dto);
        MentoringTeam mentoringTeam = mentoringTeamService.findMentoringTeam(teamId);
        TeamResponseDto teamDto = mentoringTeamService.getMentoringTeam(userId,mentoringTeam);

        return new ResultResponse<>(ResultCode.UPDATE_MENTORING_TEAM, List.of(teamDto));
    }

    @GetMapping("/teams")
    @Operation(summary = "나의 모든 멘토링 팀 조회", description = "나의 모든 멘토링 팀을 조회할 수 있다. 마이페이지에서 사용")
    public ResultResponse<MyTeamDto> findMyMentoringTeams() {
        Long userId = getUserId();
        List<MentoringTeam> myMentoringTeams = mentoringTeamService.findMyMentoringTeams(userId);
        List<MyTeamDto> teams = myMentoringTeams.stream()
                .map(team -> mentoringTeamService.getMyTeam(userId,team))
                .collect(Collectors.toList());
        return new ResultResponse<>(ResultCode.GET_MY_ALL_MENTORING_TEAM, teams);
    }

    @GetMapping("/team/{teamId}")
    @Operation(summary = "멘토링 팀 조회", description = "특정 멘토링 팀을 조회할 수 있다. 현재 로그인 된 유저의 멘토링 팀 ID들 같이 반환, 프론트에서 비교 필요(존재하면 수정하기, 삭제하기 버튼)")
    public ResultResponse<?> findMentoringTeam(@PathVariable Long teamId) {
        Long userId = getUserId();
        User user = userService.findById(userId).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        MentoringTeam mentoringTeam = mentoringTeamService.findMentoringTeam(teamId);
        TeamResponseDto teamDto = mentoringTeamService.getMentoringTeam(userId,mentoringTeam);
        return new ResultResponse<>(ResultCode.GET_MENTORING_TEAM, List.of(teamDto));
    }

    /**
     * 멘토링팀의 게시글 같이 삭제함
     * @param teamId
     * @return
     */
    @PostMapping("/team/{teamId}/del")
    @Operation(summary = "멘토링 팀 삭제", description = "특정 멘토링 팀을 삭제할 수 있다.")
    public ResultResponse<Void> deleteMentoringTeam(@PathVariable Long teamId) {
        Long userId = getUserId();
        mentoringTeamService.deleteMentoringTeam(userId, teamId);  //멘토링팀 삭제처리
        return new ResultResponse<>(ResultCode.DELETE_MENTORING_TEAM, null);
    }

    private Long getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUserDto securityUser = (SecurityUserDto) authentication.getPrincipal();
        return securityUser.getUserId();
    }
}
