package com.project.Teaming.domain.project.service;

import com.project.Teaming.domain.project.entity.ParticipationStatus;
import com.project.Teaming.domain.project.entity.ProjectParticipation;
import com.project.Teaming.domain.project.repository.ProjectParticipationRepository;
import com.project.Teaming.domain.user.entity.Report;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.repository.ReportRepository;
import com.project.Teaming.domain.user.repository.UserRepository;
import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.exception.BusinessException;
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
public class ProjectReportService {

    private final ProjectParticipationRepository projectParticipationRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final ProjectNotificationService projectNotificationService;

    private User getLoginUser() {
        return userRepository.findById(getCurrentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));
    }

    private Long getCurrentId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUserDto securityUser = (SecurityUserDto) authentication.getPrincipal();
        return securityUser.getUserId();
    }

    private ProjectParticipation getReportParticipationAbout(Long teamId, Long reportUserId) {
        return projectParticipationRepository.findByProjectTeamIdAndUserId(teamId, reportUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROJECT_PARTICIPATION));
    }

    public void reportUser(Long teamId, Long reportedUserId) {
        // 로그인 사용자 조회(신고자)
        User reporter = getLoginUser();

        // 신고자의 참여 정보 조회
        ProjectParticipation reporterParticipation = getReportParticipationAbout(teamId, reporter.getId());

        // 신고 대상의 참여 정보 조회
        ProjectParticipation reportedParticipation = projectParticipationRepository.findByProjectTeamIdAndUserId(
                        reporterParticipation.getProjectTeam().getId(), reportedUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REPORT_TARGET));

        validateNotSelfReport(reportedUserId, reporter);
        validateDuplicationReport(reporterParticipation, reportedUserId);
        Report report = Report.projectReport(reporterParticipation, reportedParticipation.getUser());
        reportRepository.save(report);

        // 받은 신고 과반수 체크 및 warningCnt 증가
        updateReportedWarningCount(reportedParticipation);
    }

    /**
     * 본인에 대한 신고 불가
     */
    private void validateNotSelfReport(Long reportedUserId, User reporter) {
        if (reporter.getId().equals(reportedUserId)) {
            throw new BusinessException(ErrorCode.INVALID_SELF_ACTION);
        }
    }

    /**
     * 중복 신고 방지
     */
    private void validateDuplicationReport(ProjectParticipation reporterParticipation, Long reportedUserId) {
        boolean isDuplicate = reportRepository.existsByProjectParticipationAndReportedUserId(reporterParticipation, reportedUserId);
        if (isDuplicate) {
            throw new BusinessException(ErrorCode.ALREADY_REPORTED);
        }
    }

    private void updateReportedWarningCount(ProjectParticipation reportedParticipation) {
        Long teamId = reportedParticipation.getProjectTeam().getId();
        Long reportedUserId = reportedParticipation.getUser().getId();

        // 팀원 수 조회
        long totalMembers = projectParticipationRepository.countByProjectTeamIdAndParticipationStatusAndIsDeleted(
                teamId, ParticipationStatus.ACCEPTED, false);

        // 신고 수 조회 (warningProcessed = false 인 신고만 조회. warningCnt 중복 증가 방지)
        long reportCount = reportRepository.countByReportedUserIdAndProjectParticipation_ProjectTeamIdAndWarningProcessedFalse(reportedUserId, teamId);

        // 과반수 이상인지 확인
        if (reportCount >= Math.ceil(totalMembers / 2.0)) {
            // warningCnt 증가
            User reportedUser = reportedParticipation.getUser();
            reportedUser.incrementWarningCnt();
            userRepository.save(reportedUser);
            projectNotificationService.warning(reportedUser);

            // 처리된 신고에 대해 warningProcess = true 설정
            reportRepository.updateWarningProcessedByReportedUserAndTeamId(reportedUserId, teamId);
        }
    }
}
