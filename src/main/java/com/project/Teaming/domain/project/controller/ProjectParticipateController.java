package com.project.Teaming.domain.project.controller;

import com.project.Teaming.domain.project.dto.request.JoinTeamDto;
import com.project.Teaming.domain.project.dto.request.ReportDto;
import com.project.Teaming.domain.project.dto.request.ReviewDto;
import com.project.Teaming.domain.project.dto.response.ProjectParticipationInfoDto;
import com.project.Teaming.domain.project.dto.response.ProjectTeamMemberDto;
import com.project.Teaming.domain.project.service.ProjectParticipationService;
import com.project.Teaming.domain.project.service.ProjectReviewService;
import com.project.Teaming.global.result.ResultCode;
import com.project.Teaming.global.result.ResultDetailResponse;
import com.project.Teaming.global.result.ResultListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@Tag(name = "ProjectParticipate", description = "프로젝트 팀 대기 목록 관련 API")
public class ProjectParticipateController {

    private final ProjectParticipationService projectParticipationService;
    private final ProjectReviewService projectReviewService;

    @PostMapping("/project/join")
    @Operation(summary = "프로젝트 팀에 신청", description = "팀원으로 들어가길 원하는 사용자가 팀에 신청을 하면 대기열에 등록된다.")
    public ResultDetailResponse<Void> joinProjectTeam(@RequestBody JoinTeamDto dto) {
        projectParticipationService.joinTeam(dto);
        return new ResultDetailResponse<>(ResultCode.JOIN_MEMBER_PROJECT_TEAM, null);
    }

    @DeleteMapping("/project/join/{team_id}/cancel")
    @Operation(summary = "프로젝트 신청 취소", description = "대기열에 있는 팀원은 원할 때, 신청 취소(status: pending, isDeleted: false 인 경우에만 가능)를 할 수 있다.")
    public ResultDetailResponse<Void> cancelProjectTeam(@PathVariable Long team_id) {
        projectParticipationService.cancelTeam(team_id);
        return new ResultDetailResponse<>(ResultCode.CANCEL_PROJECT_TEAM, null);
    }

    @PutMapping("/project/{team_id}/quit")
    @Operation(summary = "프로젝트 탈퇴", description = "프로젝트 팀의 정식 팀원이 팀에서 스스로 탈퇴할 수 있다.(만약 무단으로 나가는 경우 추후에 팀원들에게 신고를 당할 수 있다.)")
    public ResultDetailResponse<Void> quitProjectTeam(@PathVariable Long team_id) {
        projectParticipationService.quitTeam(team_id);
        return new ResultDetailResponse<>(ResultCode.QUIT_PROJECT_TEAM, null);
    }

    @PutMapping("/project/team/{team_id}/{user_id}/accept")
    @Operation(summary = "팀장의 팀원 신청 수락", description = "프로젝트 팀의 팀장은 대기열의 팀원 신청에 대하여 수락을 한다.")
    public ResultDetailResponse<Void> acceptedTeamMember(@PathVariable Long team_id, @PathVariable Long user_id) {
        projectParticipationService.acceptedMember(team_id, user_id);
        return new ResultDetailResponse<>(ResultCode.ACCEPT_JOIN_MEMBER, null);
    }

    @PutMapping("/project/team/{team_id}/{user_id}/reject")
    @Operation(summary = "팀장의 팀원 신청 거절", description = "프로젝트 팀의 팀장은 대기열의 팀원 신청에 대하여 거절을 한다.")
    public ResultDetailResponse<Void> rejectedTeamMember(@PathVariable Long team_id, @PathVariable Long user_id) {
        projectParticipationService.rejectedMember(team_id, user_id);
        return new ResultDetailResponse<>(ResultCode.REJECT_JOIN_MEMBER, null);
    }

    @GetMapping("/project/team/{team_id}/participations")
    @Operation(summary = "프로젝트 팀 지원자 목록(대기열) 조회", description = "해당 프로젝트 팀의 지원자들을 조회한다.")
    public ResultListResponse<ProjectParticipationInfoDto> getParticipations(@PathVariable Long team_id){
        List<ProjectParticipationInfoDto> list = projectParticipationService.getAllParticipationDtos(team_id);
        return  new ResultListResponse<>(ResultCode.GET_PARTICIPATION_LIST, list);
    }

    @GetMapping("/project/team/{team_id}/member")
    @Operation(summary = "프로젝트 팀의 팀원 목록 조회", description = "프로젝트 팀원을 조회한다.")
    public ResultListResponse<ProjectTeamMemberDto> getMembers(@PathVariable Long team_id) {
        List<ProjectTeamMemberDto> list = projectParticipationService.getAllMembers(team_id);
        return new ResultListResponse<>(ResultCode.GET_MEMBER_LIST, list);
    }

    @PutMapping("/project/team/{team_id}/member/{user_id}/export")
    @Operation(summary = "팀장의 팀원 내보내기", description = "팀장은 해당 팀의 팀원을 팀에서 내보낼 수 있다.")
    public ResultDetailResponse<Void> exportTeamMember(@PathVariable Long team_id, @PathVariable Long user_id) {
        projectParticipationService.exportMember(team_id, user_id);
        return new ResultDetailResponse<>(ResultCode.EXPORT_TEAM_MEMBER, null);
    }

    @PostMapping("project/report")
    @Operation(summary = "프로젝트 팀 내 팀원 신고", description = "프로젝트 내의 팀원들은 팀원에 대하여 신고를 할 수 있다.")
    public ResultDetailResponse<Void> reportUser(@RequestBody ReportDto dto) {
        projectParticipationService.reportUser(dto.getTeamId(), dto.getReportedUserId());
        return new ResultDetailResponse<>(ResultCode.REPORT_MEMBER, null);
    }

    @PostMapping("/project/review")
    @Operation(summary = "프로젝트 팀 내 팀원 리뷰 작성", description = "프로젝트 내의 팀원들은 프로젝트 종료 후 팀원에 대해서 리뷰를 작성할 수 있다.")
    public ResultDetailResponse<Void> reviewUser(@RequestBody ReviewDto dto) {
        projectReviewService.reviewUser(dto);
        return new ResultDetailResponse<>(ResultCode.REVIEW_MEMBER, null);
    }
}
