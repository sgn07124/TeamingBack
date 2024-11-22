package com.project.Teaming.domain.project.service;

import com.project.Teaming.domain.project.dto.request.CreateTeamDto;
import com.project.Teaming.domain.project.dto.request.UpdateTeamDto;
import com.project.Teaming.domain.project.dto.response.ProjectTeamInfoDto;
import com.project.Teaming.domain.project.entity.ProjectTeam;
import com.project.Teaming.domain.project.entity.RecruitCategory;
import com.project.Teaming.domain.project.entity.Stack;
import com.project.Teaming.domain.project.entity.TeamRecruitCategory;
import com.project.Teaming.domain.project.entity.TeamStack;
import com.project.Teaming.domain.project.repository.ProjectTeamRepository;
import com.project.Teaming.domain.project.repository.RecruitCategoryRepository;
import com.project.Teaming.domain.project.repository.StackRepository;
import com.project.Teaming.domain.project.repository.TeamRecruitCategoryRepository;
import com.project.Teaming.domain.project.repository.TeamStackRepository;
import java.util.List;
import java.util.stream.Collectors;
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
    private final StackRepository stackRepository;
    private final TeamStackRepository teamStackRepository;
    private final RecruitCategoryRepository recruitCategoryRepository;
    private final TeamRecruitCategoryRepository teamRecruitCategoryRepository;

    public ProjectTeam createTeam(CreateTeamDto dto) {
        ProjectTeam projectTeam = ProjectTeam.projectTeam(dto);
        projectTeamRepository.save(projectTeam);

        List<Long> stackIds = dto.getStackIds();
        List<Stack> stacks = stackRepository.findAllById(stackIds);

        for (Stack stack : stacks) {
            TeamStack teamStack = TeamStack.addStacks(projectTeam, stack);
            teamStackRepository.save(teamStack);
        }

        List<Long> recruitCategoryIds = dto.getRecruitCategoryIds();
        List<RecruitCategory> recruitCategories = recruitCategoryRepository.findAllById(recruitCategoryIds);

        for (RecruitCategory recruitCategory : recruitCategories) {
            TeamRecruitCategory teamRecruitCategory = TeamRecruitCategory.addRecruitCategories(projectTeam, recruitCategory);
            teamRecruitCategoryRepository.save(teamRecruitCategory);
        }

        return projectTeam;
    }

    public ProjectTeamInfoDto getTeam(Long teamId) {
        ProjectTeam projectTeam = projectTeamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트 팀 정보를 찾을 수 없습니다."));

        // 기술 스택 이름 리스트 생성
        List<String> stackNames = projectTeam.getStacks().stream()
                .map(teamStack -> teamStack.getStack().getStackName())
                .collect(Collectors.toList());

        List<String> recruitCategoryNames = projectTeam.getRecruitCategories().stream()
                .map(teamRecruitCategory -> teamRecruitCategory.getRecruitCategory().getName())
                .collect(Collectors.toList());

        return getProjectTeamInfoDto(projectTeam, stackNames, recruitCategoryNames);
    }

    private static ProjectTeamInfoDto getProjectTeamInfoDto(ProjectTeam projectTeam, List<String> stackNames, List<String> recruitCategoryNames) {
        ProjectTeamInfoDto dto = new ProjectTeamInfoDto();
        dto.setProjectId(projectTeam.getId());
        dto.setProjectName(projectTeam.getName());
        dto.setStartDate(projectTeam.getStartDate());
        dto.setEndDate(projectTeam.getEndDate());
        dto.setDeadline(projectTeam.getDeadline());
        dto.setMemberCnt(projectTeam.getMembersCnt());
        dto.setLink(projectTeam.getLink());
        dto.setContents(projectTeam.getContents());
        dto.setCreatedDate(projectTeam.getCreatedDate());
        dto.setLastModifiedDate(projectTeam.getLastModifiedDate());
        dto.setStacks(stackNames);
        dto.setRecruitCategories(recruitCategoryNames);
        return dto;
    }

    public void editTeam(Long teamId, UpdateTeamDto dto) {
        ProjectTeam projectTeam = projectTeamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트 팀 정보를 찾을 수 없습니다."));

        List<Stack> stacks = stackRepository.findAllById(dto.getStackIds());
        if (stacks.isEmpty()) {
            throw new IllegalArgumentException("유효하지 않은 스택 id가 포함되어 있습니다.");
        }

        List<RecruitCategory> recruitCategories = recruitCategoryRepository.findAllById(dto.getRecruitCategoryIds());
        if (recruitCategories.isEmpty()) {
            throw new IllegalArgumentException("유효하지 않은 모집 구분 id가 포함되어 있습니다.");
        }

        projectTeam.updateProjectTeam(dto);
        projectTeam.updateStacks(stacks);
        projectTeam.updateRecruitCategories(recruitCategories);
    }

    public void deleteTeam(Long teamId) {
        ProjectTeam projectTeam = projectTeamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트 팀 정보를 찾을 수 없습니다."));

        projectTeamRepository.delete(projectTeam);
    }
}
