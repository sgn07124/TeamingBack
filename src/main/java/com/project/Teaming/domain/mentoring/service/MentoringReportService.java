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
import com.project.Teaming.domain.mentoring.repository.MentoringTeamRepository;
import com.project.Teaming.domain.mentoring.service.policy.MentoringReportPolicy;
import com.project.Teaming.domain.user.entity.Report;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.repository.ReportRepository;
import com.project.Teaming.domain.user.repository.UserRepository;
import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.exception.BusinessException;
import com.project.Teaming.global.error.exception.MentoringTeamNotFoundException;
import com.project.Teaming.global.jwt.dto.SecurityUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;


@Service
@RequiredArgsConstructor
public class MentoringReportService {

    private final UserDataProvider userDataProvider;
    private final MentoringTeamDataProvider mentoringTeamDataProvider;
    private final MentoringParticipationRepository mentoringParticipationRepository;
    private final MentoringParticipationDataProvider mentoringParticipationDataProvider;
    private final ReportRepository reportRepository;
    private final MentoringReportPolicy mentoringReportPolicy;
    private final RedisParticipationManagementService redisParticipationManagementService;

    @Transactional
    public void reportTeamUser(MentoringReportRequest dto) {
        // 신고자
        User reporter = userDataProvider.getUser();

        //관련된 팀
        MentoringTeam mentoringTeam = mentoringTeamDataProvider.findMentoringTeam(dto.getTeamId());

        //신고자가 팀 구성원인지 확인
        MentoringParticipation reportingParticipation = mentoringParticipationDataProvider.findParticipationWith(
                mentoringTeam, reporter,null, MentoringParticipationStatus.ACCEPTED,
                null, () -> new BusinessException(ErrorCode.NOT_A_TEAM_USER));

        //신고당한 사용자
        User reportedUser = userDataProvider.findUser(dto.getReportedUserId());

        //신고당한 teamParticipation
        TeamUserResponse reportedParticipation = redisParticipationManagementService.getUser(mentoringTeam.getId(), reportedUser.getId());

        mentoringReportPolicy.validateExistingReport(reportingParticipation, reportedUser);
        mentoringReportPolicy.validateSelfReport(reporter, reportedUser);

        // 신고대상이 강퇴된 사용자거나, 탈퇴한 사용자인 경우 신고진행
        if (MentoringParticipationStatus.EXPORT.equals(reportedParticipation.getStatus()) || reportedParticipation.getIsDeleted()) {
            Report report = Report.mentoringReport(reportingParticipation, reportedUser);
            reportRepository.save(report);
            redisParticipationManagementService.incrementField(mentoringTeam.getId(), reportedUser.getId());
            updateReportedWarningCount(mentoringTeam.getId(), reportedUser);
        }
        else throw new BusinessException(ErrorCode.STILL_TEAM_USER);
    }

    @Transactional
    public void updateReportedWarningCount(Long mentoringTeamId, User reportedUser) {
        // Redis Hash 데이터 가져오기
        Map<String, String> reportedFields = redisParticipationManagementService.getReportFields(mentoringTeamId, reportedUser.getId());
        // 경고 처리 여부 확인
        if (Boolean.parseBoolean(reportedFields.getOrDefault("warningProcessed", "false"))) {
            return; // 이미 처리된 경우 중복 처리 방지
        }
        // 팀원 수 조회
        long totalMembers = mentoringParticipationRepository.countBy(mentoringTeamId, MentoringParticipationStatus.ACCEPTED);

        // 과반수 이상의 신고 횟수인지 확인
        if (totalMembers > 0 && Integer.parseInt(reportedFields.getOrDefault("reportedCount", "0")) >= Math.ceil(totalMembers / 2.0)) {
            reportedUser.incrementWarningCnt();
            // 경고 처리 상태 업데이트
            redisParticipationManagementService.updateWarningProcessed(mentoringTeamId, reportedUser.getId());
        }
    }
}
