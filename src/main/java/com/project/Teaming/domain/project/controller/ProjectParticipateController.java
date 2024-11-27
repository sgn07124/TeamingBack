package com.project.Teaming.domain.project.controller;

import com.project.Teaming.domain.project.service.ProjectParticipationService;
import com.project.Teaming.global.result.ResultCode;
import com.project.Teaming.global.result.ResultResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@Tag(name = "ProjectParticipate", description = "프로젝트 팀 대기 목록 관련 API")
public class ProjectParticipateController {

    private final ProjectParticipationService projectParticipationService;

    @PostMapping("/project/join/{team_id}")
    @Operation(summary = "프로젝트 팀에 신청", description = "팀원으로 들어가길 원하는 사용자가 팀에 신청을 하면 대기열에 등록된다.")
    public ResultResponse<Void> joinProjectTeam(@PathVariable Long team_id) {
        projectParticipationService.joinTeam(team_id);
        return new ResultResponse<>(ResultCode.JOIN_MEMBER_PROJECT_TEAM, null);
    }

    @DeleteMapping("/project/join/{team_id}/cancel")
    @Operation(summary = "프로젝트 신청 취소", description = "대기열에 있는 팀원은 원할 때, 신청 취소(status: pending, isDeleted: false 인 경우에만 가능)를 할 수 있다.")
    public ResultResponse<Void> cancelProjectTeam(@PathVariable Long team_id) {
        projectParticipationService.cancelTeam(team_id);
        return new ResultResponse<>(ResultCode.CANCEL_PROJECT_TEAM, null);
    }

    @PutMapping("/project/{team_id}/quit")
    @Operation(summary = "프로젝트 탈퇴", description = "프로젝트 팀의 정식 팀원이 팀에서 스스로 탈퇴할 수 있다.(만약 무단으로 나가는 경우 추후에 팀원들에게 신고를 당할 수 있다.)")
    public ResultResponse<Void> quitProjectTeam(@PathVariable Long team_id) {
        projectParticipationService.quitTeam(team_id);
        return new ResultResponse<>(ResultCode.QUIT_PROJECT_TEAM, null);
    }

    @PutMapping("/project/team/{team_id}/{user_id}/accept")
    @Operation(summary = "팀장의 팀원 신청 수락", description = "프로젝트 팀의 팀장은 대기열의 팀원 신청에 대하여 수락을 한다.")
    public ResultResponse<Void> acceptedTeamMember(@PathVariable Long team_id, @PathVariable Long user_id) {
        projectParticipationService.acceptedMember(team_id, user_id);
        return new ResultResponse<>(ResultCode.ACCEPT_JOIN_MEMBER, null);
    }

    @PutMapping("/project/team/{team_id}/{user_id}/reject")
    @Operation(summary = "팀장의 팀원 신청 거절", description = "프로젝트 팀의 팀장은 대기열의 팀원 신청에 대하여 거절을 한다.")
    public ResultResponse<Void> rejectedTeamMember(@PathVariable Long team_id, @PathVariable Long user_id) {
        projectParticipationService.rejectedMember(team_id, user_id);
        return new ResultResponse<>(ResultCode.REJECT_JOIN_MEMBER, null);
    }
}
