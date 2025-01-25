package com.project.Teaming.domain.mentoring.service.policy;

import com.project.Teaming.domain.mentoring.dto.response.TeamUserResponse;
import com.project.Teaming.domain.mentoring.entity.MentoringParticipation;
import com.project.Teaming.domain.mentoring.entity.MentoringParticipationStatus;
import com.project.Teaming.domain.mentoring.entity.MentoringStatus;
import com.project.Teaming.domain.mentoring.entity.MentoringTeam;
import com.project.Teaming.domain.mentoring.repository.MentoringParticipationRepository;
import com.project.Teaming.domain.mentoring.service.RedisTeamUserManagementService;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.repository.ReviewRepository;
import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MentoringReviewPolicy {

    private final MentoringParticipationRepository mentoringParticipationRepository;
    private final ReviewRepository reviewRepository;
    private final RedisTeamUserManagementService redisService;

    public void validateToReview(MentoringTeam mentoringTeam, User reviewedUser, MentoringParticipation reviewingParticipation) {
        validateDuplicateReview(reviewingParticipation,reviewedUser);
        validateSelfReview(reviewingParticipation.getUser(), reviewedUser);
        validateReviewEligibility(mentoringTeam, reviewedUser);
    }

    public void validateReviewEligibility(MentoringTeam mentoringTeam, User reviewedUser) {
        // Redis에서 사용자 조회
        TeamUserResponse user = redisService.getUser(mentoringTeam.getId(), reviewedUser.getId());

        if (user != null) {
            return;
        } else {
            // Redis에 없는 경우 (DB에서 조회한 일반 팀원)
            mentoringParticipationRepository.findDynamicMentoringParticipation(
                            mentoringTeam, reviewedUser, null, MentoringParticipationStatus.ACCEPTED, null)
                    .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REVIEW_TARGET));

            // 팀 상태 검증
            if (mentoringTeam.getStatus() != MentoringStatus.COMPLETE) {
                throw new BusinessException(ErrorCode.CANNOT_REVIEW);
            }
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
