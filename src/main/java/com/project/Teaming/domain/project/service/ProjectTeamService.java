package com.project.Teaming.domain.project.service;

import com.project.Teaming.domain.project.dto.request.CreateTeamDto;
import com.project.Teaming.domain.project.dto.request.UpdateTeamDto;
import com.project.Teaming.domain.project.dto.request.UpdateTeamStatusDto;
import com.project.Teaming.domain.project.dto.response.MyProjectListDto;
import com.project.Teaming.domain.project.dto.response.ProjectTeamInfoDto;
import com.project.Teaming.domain.project.entity.ParticipationStatus;
import com.project.Teaming.domain.project.entity.ProjectParticipation;
import com.project.Teaming.domain.project.entity.ProjectRole;
import com.project.Teaming.domain.project.entity.ProjectTeam;
import com.project.Teaming.domain.project.entity.RecruitCategory;
import com.project.Teaming.domain.project.entity.Stack;
import com.project.Teaming.domain.project.entity.TeamRecruitCategory;
import com.project.Teaming.domain.project.entity.TeamStack;
import com.project.Teaming.domain.project.repository.ProjectParticipationRepository;
import com.project.Teaming.domain.project.repository.ProjectTeamRepository;
import com.project.Teaming.domain.project.repository.RecruitCategoryRepository;
import com.project.Teaming.domain.project.repository.StackRepository;
import com.project.Teaming.domain.project.repository.TeamRecruitCategoryRepository;
import com.project.Teaming.domain.project.repository.TeamStackRepository;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.repository.UserRepository;
import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.exception.BusinessException;
import com.project.Teaming.global.jwt.dto.SecurityUserDto;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final ProjectParticipationRepository projectParticipationRepository;
    private final UserRepository userRepository;

    public ProjectTeam createTeam(CreateTeamDto dto) {
        ProjectTeam projectTeam = ProjectTeam.projectTeam(dto);
        projectTeamRepository.save(projectTeam);

        List<Long> stackIds = dto.getStackIds();
        List<Stack> stacks = stackRepository.findAllById(stackIds);

        // 누락된 기술 스택 ID 검증
        List<Long> missingStackIds = stackIds.stream()
                .filter(id -> stacks.stream().noneMatch(stack -> stack.getId().equals(id)))
                .collect(Collectors.toList());
        if (!missingStackIds.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_VALID_STACK_ID);
        }

        for (Stack stack : stacks) {
            TeamStack teamStack = TeamStack.addStacks(projectTeam, stack);
            teamStackRepository.save(teamStack);
        }

        List<Long> recruitCategoryIds = dto.getRecruitCategoryIds();
        List<RecruitCategory> recruitCategories = recruitCategoryRepository.findAllById(recruitCategoryIds);

        // 누락된 모집 카테고리 ID 검증
        List<Long> missingRecruitCategoryIds = recruitCategoryIds.stream()
                .filter(id -> recruitCategories.stream().noneMatch(category -> category.getId().equals(id)))
                .collect(Collectors.toList());
        if (!missingRecruitCategoryIds.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_VALID_RECRUIT_CATEGORY_ID);
        }

        for (RecruitCategory recruitCategory : recruitCategories) {
            TeamRecruitCategory teamRecruitCategory = TeamRecruitCategory.addRecruitCategories(projectTeam, recruitCategory);
            teamRecruitCategoryRepository.save(teamRecruitCategory);
        }

        return projectTeam;
    }

    public ProjectTeamInfoDto getTeam(Long teamId) {
        ProjectTeam projectTeam = findProjectTeamById(teamId);

        // 기술 스택 id 리스트 생성
        List<String> stackIds = projectTeam.getStacks().stream()
                .map(teamStack -> String.valueOf(teamStack.getStack().getId()))
                .collect(Collectors.toList());

        // 모집 구분 id 리스트 생성
        List<String> recruitCategoryIds = projectTeam.getRecruitCategories().stream()
                .map(teamRecruitCategory -> String.valueOf(teamRecruitCategory.getRecruitCategory().getId()))
                .collect(Collectors.toList());

        return ProjectTeamInfoDto.from(projectTeam, stackIds, recruitCategoryIds);
    }



    public void editTeam(Long teamId, UpdateTeamDto dto) {
        ProjectTeam projectTeam = findProjectTeamById(teamId);

        List<Stack> stacks = stackRepository.findAllById(dto.getStackIds());
        List<Long> missingStackIds = dto.getStackIds().stream()
                .filter(id -> stacks.stream().noneMatch(stack -> stack.getId().equals(id)))
                .collect(Collectors.toList());
        if (!missingStackIds.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_VALID_STACK_ID);
        }

        List<RecruitCategory> recruitCategories = recruitCategoryRepository.findAllById(dto.getRecruitCategoryIds());
        List<Long> missingCategoryIds = dto.getRecruitCategoryIds().stream()
                .filter(id -> recruitCategories.stream().noneMatch(category -> category.getId().equals(id)))
                .collect(Collectors.toList());
        if (!missingCategoryIds.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_VALID_RECRUIT_CATEGORY_ID);
        }

        projectTeam.updateProjectTeam(dto);
        projectTeam.updateStacks(stacks);
        projectTeam.updateRecruitCategories(recruitCategories);
    }

    public void deleteTeam(Long teamId) {
        ProjectTeam projectTeam = findProjectTeamById(teamId);

        projectTeamRepository.delete(projectTeam);
    }

    public void updateTeamStatus(UpdateTeamStatusDto dto) {
        User user = userRepository.findById(getCurrentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));
        ProjectParticipation teamOwner = projectParticipationRepository.findByProjectTeamIdAndRole(dto.getTeamId(), ProjectRole.OWNER)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROJECT_OWNER));
        ProjectTeam projectTeam = findProjectTeamById(dto.getTeamId());

        if (user.getId().equals(teamOwner.getUser().getId())) {
            projectTeam.updateTeamStatus(dto.getStatus());
        } else {
            throw new BusinessException(ErrorCode.FAIL_TO_UPDATE_TEAM_STATUS);
        }
    }

    private Long getCurrentId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUserDto securityUser = (SecurityUserDto) authentication.getPrincipal();
        return securityUser.getUserId();
    }

    public List<MyProjectListDto> getProjectList() {
        User user = userRepository.findById(getCurrentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));
        List<ProjectParticipation> participations = projectParticipationRepository.findByUserIdAndParticipationStatus(user.getId(),
                ParticipationStatus.ACCEPTED);

        List<MyProjectListDto> projectLists = participations.stream()
                .map(participation -> {
                    ProjectTeam projectTeam = projectTeamRepository.findById(participation.getProjectTeam().getId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROJECT_TEAM));
                    return MyProjectListDto.from(projectTeam, participation);
                })
                .collect(Collectors.toList());
        return projectLists;
    }

    private ProjectTeam findProjectTeamById(Long teamId) {
        return projectTeamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROJECT_TEAM));
    }
}
