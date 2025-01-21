package com.project.Teaming.domain.mentoring.service.policy;

import com.project.Teaming.domain.mentoring.dto.response.TeamUserResponse;
import com.project.Teaming.domain.mentoring.entity.MentoringParticipation;
import com.project.Teaming.domain.mentoring.entity.MentoringParticipationStatus;
import com.project.Teaming.domain.mentoring.entity.MentoringStatus;
import com.project.Teaming.domain.mentoring.entity.MentoringTeam;
import com.project.Teaming.domain.mentoring.repository.MentoringParticipationRepository;
import com.project.Teaming.domain.mentoring.service.RedisParticipationManagementService;
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
    private final RedisParticipationManagementService redisService;

    public void validateToReview(MentoringTeam mentoringTeam, User reviewedUser, MentoringParticipation reviewingParticipation) {
        validateDuplicateReview(reviewingParticipation,reviewedUser);
        validateSelfReview(reviewingParticipation.getUser(), reviewedUser);
        validateReviewedParticipation(mentoringTeam, reviewedUser);
        validateTeamAndUser(mentoringTeam,reviewedUser);
    }

    public void validateReviewedParticipation(MentoringTeam mentoringTeam, User reviewedUser) {
        mentoringParticipationRepository.findDynamicMentoringParticipation(
                        mentoringTeam, reviewedUser, null, null, List.of(MentoringParticipationStatus.ACCEPTED, MentoringParticipationStatus.EXPORT))
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REVIEW_TARGET));
    }

    public void validateTeamAndUser(MentoringTeam mentoringTeam,User reviewdUser) {
        TeamUserResponse user = redisService.getUser(mentoringTeam.getId(), reviewdUser.getId());
        // Redis 데이터가 없고 팀 상태가 COMPLETE이 아니면 예외 처리
        if (user == null && mentoringTeam.getStatus() != MentoringStatus.COMPLETE) {
            throw new BusinessException(ErrorCode.CANNOT_REVIEW);
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
