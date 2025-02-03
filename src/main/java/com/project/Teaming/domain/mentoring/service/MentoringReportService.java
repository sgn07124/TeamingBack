package com.project.Teaming.domain.mentoring.service;

import com.project.Teaming.domain.mentoring.dto.request.MentoringReportRequest;
import com.project.Teaming.domain.mentoring.dto.response.TeamUserResponse;
import com.project.Teaming.domain.mentoring.entity.MentoringParticipation;
import com.project.Teaming.domain.mentoring.entity.MentoringParticipationStatus;
import com.project.Teaming.domain.mentoring.entity.MentoringTeam;
import com.project.Teaming.domain.mentoring.provider.MentoringParticipationDataProvider;
import com.project.Teaming.domain.mentoring.provider.MentoringTeamDataProvider;
import com.project.Teaming.domain.mentoring.provider.UserDataProvider;
import com.project.Teaming.domain.mentoring.repository.MentoringParticipationRepository;
import com.project.Teaming.domain.mentoring.service.policy.MentoringReportPolicy;
import com.project.Teaming.domain.user.entity.Report;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.repository.ReportRepository;
import com.project.Teaming.domain.user.repository.UserRepository;
import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class MentoringReportService {

    private final UserDataProvider userDataProvider;
    private final UserRepository userRepository;
    private final MentoringTeamDataProvider mentoringTeamDataProvider;
    private final MentoringParticipationRepository mentoringParticipationRepository;
    private final MentoringParticipationDataProvider mentoringParticipationDataProvider;
    private final ReportRepository reportRepository;
    private final MentoringReportPolicy mentoringReportPolicy;
    private final RedisTeamUserManagementService redisParticipationManagementService;

    @Transactional
    public void reportTeamUser(MentoringReportRequest dto) {
        log.info("Starting report process for teamId: {}, reportedUserId: {}", dto.getTeamId(), dto.getReportedUserId());
        // 신고자
        User reporter = userDataProvider.getUser();

        //관련된 팀
        MentoringTeam mentoringTeam = mentoringTeamDataProvider.findMentoringTeam(dto.getTeamId());

        //신고자가 팀 구성원인지 확인
        MentoringParticipation reportingParticipation = mentoringParticipationDataProvider.findParticipationWith(
                mentoringTeam, reporter,null, MentoringParticipationStatus.ACCEPTED,
                () -> new BusinessException(ErrorCode.NOT_A_TEAM_USER));

        //신고당한 사용자
        User reportedUser = userDataProvider.findUser(dto.getReportedUserId());

        //신고당한 teamParticipation
        TeamUserResponse reportedParticipation = redisParticipationManagementService.getUser(mentoringTeam.getId(), reportedUser.getId());

        // 검증
        mentoringReportPolicy.validateParticipationExists(reportedParticipation);
        mentoringReportPolicy.validateExistingReport(reportingParticipation, reportedUser);
        mentoringReportPolicy.validateSelfReport(reporter, reportedUser);

        //신고
        Report report = Report.mentoringReport(reportingParticipation, reportedUser);
        reportRepository.save(report);
        redisParticipationManagementService.incrementField(mentoringTeam.getId(), reportedUser.getId());
        updateReportedWarningCount(mentoringTeam.getId(), reportedUser);

    }

    @Transactional
    public void updateReportedWarningCount(Long mentoringTeamId, User reportedUser) {

        log.info("Updating warning count for teamId: {}, userId: {}", mentoringTeamId, reportedUser.getId());

        // Redis Hash 데이터 가져오기
        Map<String, String> reportedFields = redisParticipationManagementService.getReportFields(mentoringTeamId, reportedUser.getId());

        // 경고 처리 여부 확인
        if (Boolean.parseBoolean(reportedFields.getOrDefault("warningProcessed", "false"))) {
            return;
        }

        // 팀원 수 조회
        long totalMembers = mentoringParticipationRepository.countBy(mentoringTeamId, MentoringParticipationStatus.ACCEPTED);

        if (totalMembers == 0) {
            log.warn("No members in the team. Skipping warning update for teamId: {}", mentoringTeamId);
            return; // 팀원이 없는 경우 처리 중단
        }

        // 신고 횟수 확인
        int reportedCount = parseIntOrDefault(reportedFields.get("reportedCount"), 0);
        log.info("Reported count for user {}: {}", reportedUser.getId(), reportedCount);

        // 과반수 이상의 신고 횟수인지 확인
        if (reportedCount >= (totalMembers + 1) / 2) {

            reportedUser.incrementWarningCnt();
            userRepository.save(reportedUser);
            log.info("Warning count updated in DB for userId: {}", reportedUser.getId());
            // 경고 처리 상태 업데이트
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    try {
                        redisParticipationManagementService.updateWarningProcessed(mentoringTeamId, reportedUser.getId());
                        log.info("Redis warningProcessed updated for teamId: {}, userId: {}", mentoringTeamId, reportedUser.getId());
                    } catch (Exception e) {
                        log.error("Failed to update Redis for teamId: {}, userId: {}", mentoringTeamId, reportedUser.getId(), e);
                    }
                }

                @Override
                public void afterCompletion(int status) {
                    if (status != STATUS_COMMITTED) {
                        log.error("Transaction failed. Redis update aborted for teamId: {}, userId: {}", mentoringTeamId, reportedUser.getId());
                    }
                }
            });
        } else {
            log.debug("Reported count is less than majority for userId: {}", reportedUser.getId());
        }
    }

    private int parseIntOrDefault(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
