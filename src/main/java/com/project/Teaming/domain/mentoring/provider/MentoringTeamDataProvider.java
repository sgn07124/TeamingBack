package com.project.Teaming.domain.mentoring.provider;

import com.project.Teaming.domain.mentoring.entity.MentoringTeam;
import com.project.Teaming.domain.mentoring.repository.MentoringTeamRepository;
import com.project.Teaming.global.error.exception.MentoringTeamNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MentoringTeamDataProvider {

    private final MentoringTeamRepository mentoringTeamRepository;

    public MentoringTeam findMentoringTeam(Long teamId) {
        return mentoringTeamRepository.findById(teamId)
                .orElseThrow(MentoringTeamNotFoundException::new);
    }
}
