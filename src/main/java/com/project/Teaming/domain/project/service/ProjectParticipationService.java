package com.project.Teaming.domain.project.service;

import com.project.Teaming.domain.project.entity.ProjectParticipation;
import com.project.Teaming.domain.project.entity.ProjectRole;
import com.project.Teaming.domain.project.entity.ProjectTeam;
import com.project.Teaming.domain.project.repository.ProjectParticipationRepository;
import com.project.Teaming.domain.project.repository.ProjectTeamRepository;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.repository.UserRepository;
import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.exception.BusinessException;
import com.project.Teaming.global.jwt.dto.SecurityUserDto;
import java.util.Optional;
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
public class ProjectParticipationService {

    private final ProjectParticipationRepository projectParticipationRepository;
    private final ProjectTeamRepository projectTeamRepository;
    private final UserRepository userRepository;

    public void createParticipation(ProjectTeam projectTeam) {
        ProjectParticipation projectParticipation = new ProjectParticipation();
        User user = userRepository.findById(getCurrentId())
                        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        projectParticipation.createProjectParticipation(user, projectTeam);
        projectParticipationRepository.save(projectParticipation);
    }

    private Long getCurrentId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUserDto securityUser = (SecurityUserDto) authentication.getPrincipal();
        return securityUser.getUserId();
    }

    public void joinTeam(Long teamId) {
        ProjectTeam projectTeam = projectTeamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROJECT_TEAM));

        User user = userRepository.findById(getCurrentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        // 이미 팀에 참여했는지 여부 확인
        Optional<ProjectParticipation> existingParticipation = projectParticipationRepository.findByProjectTeamIdAndUserId(teamId, user.getId());
        if (existingParticipation.isPresent()) {
            ProjectParticipation participation = existingParticipation.get();
            if (participation.getRole() == ProjectRole.OWNER) {
                throw new BusinessException(ErrorCode.ALREADY_PARTICIPATED_OWNER);
            } else if (participation.getRole() == ProjectRole.MEMBER) {
                throw new BusinessException(ErrorCode.ALREADY_PARTICIPATED_MEMBER);
            }
        }

        // 새로운 팀에 참여
        ProjectParticipation newParticipation = new ProjectParticipation();
        newParticipation.joinTeamMember(user, projectTeam);
        projectParticipationRepository.save(newParticipation);
    }
}
