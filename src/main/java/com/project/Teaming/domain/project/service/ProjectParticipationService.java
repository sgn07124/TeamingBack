package com.project.Teaming.domain.project.service;

import com.project.Teaming.domain.project.entity.ProjectParticipation;
import com.project.Teaming.domain.project.entity.ProjectTeam;
import com.project.Teaming.domain.project.repository.ProjectParticipationRepository;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.repository.UserRepository;
import com.project.Teaming.global.jwt.dto.SecurityUserDto;
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
}
