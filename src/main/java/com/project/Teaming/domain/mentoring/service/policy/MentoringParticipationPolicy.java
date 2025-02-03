package com.project.Teaming.domain.mentoring.service.policy;


import com.project.Teaming.domain.mentoring.entity.MentoringAuthority;
import com.project.Teaming.domain.mentoring.entity.MentoringParticipation;
import com.project.Teaming.domain.mentoring.entity.MentoringParticipationStatus;
import com.project.Teaming.domain.mentoring.entity.MentoringTeam;
import com.project.Teaming.domain.mentoring.repository.MentoringParticipationRepository;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.exception.BusinessException;
import com.project.Teaming.global.error.exception.MentoringParticipationAlreadyExistException;
import com.project.Teaming.global.error.exception.NoAuthorityException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class MentoringParticipationPolicy {

    private final MentoringParticipationRepository mentoringParticipationRepository;

    public void validateParticipation(MentoringTeam mentoringTeam, User user, MentoringAuthority authority,
                                      MentoringParticipationStatus status,
                                      Supplier<BusinessException> exceptionSupplier) {

        mentoringParticipationRepository.findDynamicMentoringParticipation(mentoringTeam, user, authority, status)
                .orElseThrow(exceptionSupplier);
    }

    public void validateParticipationStatus(MentoringParticipation participation) {
        switch (participation.getParticipationStatus()) {
            case ACCEPTED -> throw new MentoringParticipationAlreadyExistException(ErrorCode.ALREADY_MEMBER_OF_TEAM);
            case EXPORT -> throw new NoAuthorityException(ErrorCode.EXPORTED_BY_TEAM);
            default -> throw new MentoringParticipationAlreadyExistException(ErrorCode.ALREADY_PARTICIPATED);
        }
    }

    public void validateCancellation(MentoringParticipation participation) {
        switch (participation.getParticipationStatus()) {
            case PENDING -> {
            }
            case ACCEPTED -> throw new NoAuthorityException(ErrorCode.ALREADY_MEMBER_OF_TEAM);
            case EXPORT -> throw new NoAuthorityException(ErrorCode.EXPORTED_BY_TEAM);
            default -> throw new NoAuthorityException(ErrorCode.REJECTED_FROM_MENTORING_TEAM);
        }
    }

    public void validateParticipationStatusForAcceptance(MentoringParticipation participation) {
        if (participation.getAuthority() != MentoringAuthority.NoAuth) {
            throw new NoAuthorityException(ErrorCode.STATUS_IS_NOT_PENDING);
        }
        if (participation.getParticipationStatus() != MentoringParticipationStatus.PENDING) {
            throw new NoAuthorityException(ErrorCode.STATUS_IS_NOT_PENDING);
        }
    }
}
