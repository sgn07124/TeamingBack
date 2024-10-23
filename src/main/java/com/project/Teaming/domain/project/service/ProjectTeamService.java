package com.project.Teaming.domain.project.service;

import com.project.Teaming.domain.project.dto.request.CreateTeamDto;
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
}
