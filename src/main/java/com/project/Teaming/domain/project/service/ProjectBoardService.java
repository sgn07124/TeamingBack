package com.project.Teaming.domain.project.service;

import com.project.Teaming.domain.project.dto.request.CreatePostDto;
import com.project.Teaming.domain.project.dto.response.ProjectPostInfoDto;
import com.project.Teaming.domain.project.entity.ParticipationStatus;
import com.project.Teaming.domain.project.entity.ProjectBoard;
import com.project.Teaming.domain.project.entity.ProjectTeam;
import com.project.Teaming.domain.project.repository.ProjectBoardRepository;
import com.project.Teaming.domain.project.repository.ProjectParticipationRepository;
import com.project.Teaming.domain.project.repository.ProjectTeamRepository;
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
public class ProjectBoardService {

    private final ProjectBoardRepository projectBoardRepository;
    private final ProjectTeamRepository projectTeamRepository;
    private final UserRepository userRepository;
    private final ProjectParticipationRepository projectParticipationRepository;

    private Long getCurrentId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUserDto securityUser = (SecurityUserDto) authentication.getPrincipal();
        return securityUser.getUserId();
    }

    public void createPost(Long teamId, CreatePostDto createPostDto) {
        ProjectTeam projectTeam = projectTeamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROJECT_TEAM));

        User user = userRepository.findById(getCurrentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        boolean isMember = projectParticipationRepository.existsByProjectTeamIdAndUserIdAndParticipationStatus(teamId, user.getId(),
                ParticipationStatus.ACCEPTED);
        if (!isMember) {
            throw new BusinessException(ErrorCode.USER_NOT_PART_OF_TEAM);
        }
        ProjectBoard post = ProjectBoard.projectBoard(createPostDto, projectTeam);

        projectBoardRepository.save(post);
    }

    public ProjectPostInfoDto getPostInfo(Long teamId, Long postId) {
        ProjectTeam projectTeam = projectTeamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROJECT_TEAM));

        ProjectBoard projectBoard = projectBoardRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROJECT_POST));

        List<String> stackNames = projectTeam.getStacks().stream()
                .map(teamStack -> teamStack.getStack().getStackName())
                .collect(Collectors.toList());

        List<String> recruitCategoryNames = projectTeam.getRecruitCategories().stream()
                .map(teamRecruitCategory -> teamRecruitCategory.getRecruitCategory().getName())
                .collect(Collectors.toList());

        return ProjectPostInfoDto.from(projectTeam, projectBoard, stackNames, recruitCategoryNames);
    }
}
