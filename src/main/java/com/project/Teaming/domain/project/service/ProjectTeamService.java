package com.project.Teaming.domain.project.service;

import com.project.Teaming.domain.project.dto.request.CreateTeamDto;
import com.project.Teaming.domain.project.dto.request.UpdateTeamDto;
import com.project.Teaming.domain.project.dto.response.ProjectTeamInfoDto;
import com.project.Teaming.domain.project.entity.ProjectTeam;
import com.project.Teaming.domain.project.repository.ProjectTeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProjectTeamService {

    private final ProjectTeamRepository projectTeamRepository;

    public ProjectTeam createTeam(CreateTeamDto dto) {
        ProjectTeam projectTeam = ProjectTeam.projectTeam(dto);
        return projectTeamRepository.save(projectTeam);

    }

    public ProjectTeamInfoDto getTeam(Long teamId) {
        ProjectTeam projectTeam = projectTeamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트 팀 정보를 찾을 수 없습니다."));

        ProjectTeamInfoDto dto = new ProjectTeamInfoDto();
        dto.setProjectId(projectTeam.getId());
        dto.setProjectName(projectTeam.getName());
        dto.setStartDate(projectTeam.getStartDate());
        dto.setEndDate(projectTeam.getEndDate());
        dto.setMemberCnt(projectTeam.getMembersCnt());
        dto.setLink(projectTeam.getLink());
        dto.setContents(projectTeam.getContents());
        dto.setCreatedDate(projectTeam.getCreatedDate());
        dto.setLastModifiedDate(projectTeam.getLastModifiedDate());
        return dto;
    }

    public void editTeam(Long teamId, UpdateTeamDto dto) {
        ProjectTeam projectTeam = projectTeamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트 팀 정보를 찾을 수 없습니다."));

        projectTeam.updateProjectTeam(dto);
    }

    public void deleteTeam(Long teamId) {
        ProjectTeam projectTeam = projectTeamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트 팀 정보를 찾을 수 없습니다."));

        projectTeamRepository.delete(projectTeam);
    }
}
