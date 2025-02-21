package com.project.Teaming.domain.mentoring.provider;

import com.project.Teaming.domain.mentoring.entity.MentoringParticipation;
import com.project.Teaming.domain.mentoring.entity.MentoringParticipationStatus;
import com.project.Teaming.domain.mentoring.entity.MentoringTeam;
import com.project.Teaming.domain.mentoring.repository.MentoringParticipationRepository;
import com.project.Teaming.domain.mentoring.repository.MentoringTeamRepository;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.global.error.exception.MentoringTeamNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MentoringTeamDataProvider {

    private final MentoringTeamRepository mentoringTeamRepository;
    private final MentoringParticipationRepository mentoringParticipationRepository;

    public MentoringTeam findMentoringTeam(Long teamId) {
        return mentoringTeamRepository.findById(teamId)
                .orElseThrow(MentoringTeamNotFoundException::new);
    }

    public List<MentoringTeam> getTeamsByUserAndStatus(User user, MentoringParticipationStatus participationStatus) {
        return mentoringParticipationRepository.findParticipationWithStatusAndUser(user,participationStatus)
                .stream()
                .map(MentoringParticipation::getMentoringTeam)
                .toList();
    }
}
