package com.project.Teaming.domain.mentoring.provider;

import com.project.Teaming.domain.mentoring.entity.MentoringAuthority;
import com.project.Teaming.domain.mentoring.entity.MentoringParticipation;
import com.project.Teaming.domain.mentoring.entity.MentoringParticipationStatus;
import com.project.Teaming.domain.mentoring.entity.MentoringTeam;
import com.project.Teaming.domain.mentoring.repository.MentoringParticipationRepository;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.global.error.exception.BusinessException;
import com.project.Teaming.global.error.exception.MentoringParticipationNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class MentoringParticipationDataProvider {

    private final MentoringParticipationRepository mentoringParticipationRepository;

    public MentoringParticipation findParticipation(Long participationId) {
        return mentoringParticipationRepository.findById(participationId)
                .orElseThrow(MentoringParticipationNotFoundException::new);
    }

    public MentoringParticipation findParticipationWith(MentoringTeam mentoringTeam, User user, MentoringAuthority authority,
                                                        MentoringParticipationStatus status, List<MentoringParticipationStatus> statuses,
                                                        Boolean isDeleted,
                                                        Supplier<BusinessException> exceptionSupplier) {

        return mentoringParticipationRepository.findDynamicMentoringParticipation(mentoringTeam, user, authority, status, statuses, isDeleted)
                .orElseThrow(exceptionSupplier);
    }
}
