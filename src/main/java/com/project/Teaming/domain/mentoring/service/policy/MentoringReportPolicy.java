package com.project.Teaming.domain.mentoring.service.policy;

import com.project.Teaming.domain.mentoring.entity.MentoringParticipation;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.repository.ReportRepository;
import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MentoringReportPolicy {

    private final ReportRepository reportRepository;

    public void validateExistingReport(MentoringParticipation reportingParticipation, User reportedUser) {
        boolean reportExists = reportRepository.existsByMentoringParticipationAndReportedUser(reportingParticipation, reportedUser);
        if (reportExists) {
            throw new BusinessException(ErrorCode.ALREADY_REPORTED);
        }
    }

    public void validateSelfReport(User reporter, User reportedUser) {
        if (reporter.getId().equals(reportedUser.getId())) {
            throw new BusinessException(ErrorCode.INVALID_SELF_ACTION);
        }
    }
}
