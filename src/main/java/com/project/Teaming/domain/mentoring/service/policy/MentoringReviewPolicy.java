package com.project.Teaming.domain.mentoring.service.policy;

import com.project.Teaming.domain.mentoring.entity.MentoringParticipation;
import com.project.Teaming.domain.mentoring.entity.MentoringParticipationStatus;
import com.project.Teaming.domain.mentoring.entity.MentoringStatus;
import com.project.Teaming.domain.mentoring.entity.MentoringTeam;
import com.project.Teaming.domain.mentoring.repository.MentoringParticipationRepository;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.repository.ReviewRepository;
import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MentoringReviewPolicy {

    private final MentoringParticipationRepository mentoringParticipationRepository;
    private final ReviewRepository reviewRepository;

    public void validateToReview(MentoringTeam mentoringTeam, User reviewedUser, MentoringParticipation reviewingParticipation) {
        validateDuplicateReview(reviewingParticipation,reviewedUser);
        validateSelfReview(reviewingParticipation.getUser(), reviewedUser);
        validateReviewedParticipation(mentoringTeam, reviewedUser);
        validateMentoringTeamStatus(mentoringTeam);
    }

    public void validateReviewedParticipation(MentoringTeam mentoringTeam, User reviewedUser) {
        mentoringParticipationRepository.findDynamicMentoringParticipation(
                        mentoringTeam, reviewedUser, null, null, List.of(MentoringParticipationStatus.ACCEPTED, MentoringParticipationStatus.EXPORT), null)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REVIEW_TARGET));
    }

    public void validateMentoringTeamStatus(MentoringTeam mentoringTeam) {
        if (mentoringTeam.getStatus() != MentoringStatus.COMPLETE) {
            throw new BusinessException(ErrorCode.STILL_IN_PROGRESS);
        }
    }

    public void validateDuplicateReview(MentoringParticipation reviewingParticipation, User reviewedUser) {
        if (reviewRepository.existsByMentoringParticipationAndReviewee(reviewingParticipation, reviewedUser)) {
            throw new BusinessException(ErrorCode.ALREADY_REVIEWED);
        }
    }

    public void validateSelfReview(User reviewer, User reviewee) {
        if (reviewer.equals(reviewee)) {
            throw new BusinessException(ErrorCode.INVALID_SELF_ACTION);
        }
    }

}
