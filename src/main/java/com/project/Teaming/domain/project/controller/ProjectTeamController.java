package com.project.Teaming.domain.project.controller;

import com.project.Teaming.domain.project.dto.request.CreateTeamDto;
import com.project.Teaming.domain.project.dto.request.UpdateTeamDto;
import com.project.Teaming.domain.project.dto.request.UpdateTeamStatusDto;
import com.project.Teaming.domain.project.dto.response.MyProjectListDto;
import com.project.Teaming.domain.project.dto.response.ProjectTeamInfoDto;
import com.project.Teaming.domain.project.entity.ProjectTeam;
import com.project.Teaming.domain.project.service.ProjectParticipationService;
import com.project.Teaming.domain.project.service.ProjectTeamService;
import com.project.Teaming.global.result.ResultCode;
import com.project.Teaming.global.result.ResultDetailResponse;
import com.project.Teaming.global.result.ResultListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@Tag(name = "ProjectTeam", description = "프로젝트 팀 관련 API")
public class ProjectTeamController {

    private final ProjectTeamService projectTeamService;
    private final ProjectParticipationService projectParticipationService;

    @PostMapping("/project/team")
    @Operation(summary = "프로젝트 팀 생성", description = "프로젝트를 생성하고 싶은 사용자는 프로젝트 팀 생성을 통해 프로젝트를 생성할 수 있고 해당 프로젝트의 팀장이 된다. \n기술 스택과 모집 구분은 [1, 2]와 같이 리스트 형태로 작성한다.(project_stack과 project_recruit_category의 id 값")
    public ResultDetailResponse<Void> createTeam(@RequestBody @Valid CreateTeamDto createTeamDto) {
        ProjectTeam projectTeam = projectTeamService.createTeam(createTeamDto);
        projectParticipationService.createParticipation(projectTeam);
        return new ResultDetailResponse<>(ResultCode.REGISTER_PROJECT_TEAM, null);
    }

    @GetMapping("/project/team/{team_id}")
    @Operation(summary = "프로젝트 팀 정보 조회", description = "프로젝트 팀의 정보를 조회한다.")
    public ResultDetailResponse<ProjectTeamInfoDto> getTeam(@PathVariable Long team_id) {
        ProjectTeamInfoDto dto = projectTeamService.getTeam(team_id);
        return new ResultDetailResponse<>(ResultCode.GET_PROJECT_TEAM, dto);
    }

    @PutMapping("/project/team/{team_id}/edit")
    @Operation(summary = "프로젝트 팀 정보 수정", description = "프로젝트 팀의 정보를 수정한다.")
    public ResultDetailResponse<Void> editTeam(@PathVariable Long team_id, @RequestBody UpdateTeamDto updateTeamDto) {
        projectTeamService.editTeam(team_id, updateTeamDto);
        return new ResultDetailResponse<>(ResultCode.UPDATE_PROJECT_TEAM, null);
    }

    @DeleteMapping("project/team/{team_id}/delete")
    @Operation(summary = "프로젝트 팀 삭제", description = "프로젝트 팀을 삭제한다.")
    public ResultDetailResponse<Void> deleteTeam(@PathVariable Long team_id) {
        projectTeamService.deleteTeam(team_id);
        return new ResultDetailResponse<>(ResultCode.DELETE_PROJECT_TEAM, null);
    }

    @PutMapping("/project/team/status")
    @Operation(summary = "팀장의 팀 상태 변경", description = "팀장은 팀의 상태(모집 중, 진행 중, 완료)를 변경할 수 있다.")
    public ResultDetailResponse<Void> editTeamStatus(@RequestBody @Valid UpdateTeamStatusDto dto) {
        projectTeamService.updateTeamStatus(dto);
        return new ResultDetailResponse<>(ResultCode.UPDATE_TEAM_STATUS, null);
    }

    @GetMapping("/user/project")
    @Operation(summary = "마이페이지의 참여 프로젝트 목록 조회", description = "로그인한 사용자는 본인의 마이페이지에서 본인이 참여했던 프로젝트 목록을 조회할 수 있다.")
    public ResultListResponse<MyProjectListDto> getProjectTeamList() {
        List<MyProjectListDto> teams = projectTeamService.getProjectList();
        return new ResultListResponse<>(ResultCode.GET_MY_PROJECT, teams);
    }

    @GetMapping("/users/{userId}/project")
    @Operation(summary = "특정 유저의 참여 프로젝트 목록 조회", description = "특정 유저의 프로필에서 프로젝트 참여 정보를 조회할 수 있다.")
    public ResultListResponse<MyProjectListDto> getSpecificProjectTeamList(@PathVariable Long userId) {
        List<MyProjectListDto> teams = projectTeamService.getSpecificProjectList(userId);
        return new ResultListResponse<>(ResultCode.GET_MY_PROJECT, teams);
    }
}
