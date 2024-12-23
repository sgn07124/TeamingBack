package com.project.Teaming.domain.mentoring.service;

import com.project.Teaming.domain.mentoring.dto.request.MentoringReportDto;
import com.project.Teaming.domain.mentoring.entity.MentoringParticipation;
import com.project.Teaming.domain.mentoring.entity.MentoringParticipationStatus;
import com.project.Teaming.domain.mentoring.entity.MentoringTeam;
import com.project.Teaming.domain.mentoring.repository.MentoringParticipationRepository;
import com.project.Teaming.domain.mentoring.repository.MentoringTeamRepository;
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


@Service
@RequiredArgsConstructor
public class MentoringReportService {

    private final UserRepository userRepository;
    private final MentoringTeamRepository mentoringTeamRepository;
    private final MentoringParticipationRepository mentoringParticipationRepository;
    private final ReportRepository reportRepository;

    @Transactional
    public void reportTeamUser(MentoringReportDto dto) {
        // 신고자
        User reporter = getUser();
        //관련된 팀
        MentoringTeam mentoringTeam = mentoringTeamRepository.findById(dto.getTeamId())
                .orElseThrow(MentoringTeamNotFoundException::new);
        //신고한 teamParticipation
        //신고자가 팀 구성원인지 확인
        MentoringParticipation reportingParticipation = mentoringParticipationRepository.findByMentoringTeamAndUserAndParticipationStatus(mentoringTeam, reporter, MentoringParticipationStatus.ACCEPTED)
                .orElseThrow(() ->new BusinessException(ErrorCode.NOT_A_TEAM_USER));
        //신고당한 사용자
        User reportedUser = userRepository.findById(dto.getReportedUserId())
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        //신고당한 teamParticipation
        MentoringParticipation reportedParticipation = mentoringParticipationRepository.findByMentoringTeamAndUser(mentoringTeam, reportedUser)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REPORT_TARGET));

        // 이미 신고한 사용자인지 확인
        boolean reportExists = reportRepository.existsByMentoringParticipationAndReportedUser(reportingParticipation, reportedUser);
        if (reportExists) {
            throw new BusinessException(ErrorCode.ALREADY_REPORTED);
        }
        // 자기자신에 대해서 하는 경우 예외처리
        if (reporter.getId().equals(dto.getReportedUserId())) {
            throw new BusinessException(ErrorCode.INVALID_SELF_ACTION);
        }
        // 신고대상이 강퇴된 사용자거나, 탈퇴한 사용자인 경우 신고진행
        if (reportedParticipation.getParticipationStatus() == MentoringParticipationStatus.EXPORT || reportedParticipation.getIsDeleted()) {
            Report report = Report.mentoringReport(reportingParticipation, reportedUser);
            reportRepository.save(report);
            reportedParticipation.setReportingCnt(reportedParticipation.getReportingCnt() + 1);
            updateReportedWarningCount(reportedParticipation);
        }
        else throw new BusinessException(ErrorCode.STILL_TEAM_USER);
    }

    @Transactional
    public void updateReportedWarningCount(MentoringParticipation reportedParticipation) {
        Long teamId = reportedParticipation.getMentoringTeam().getId();

        // 경고 처리 여부 확인
        if (reportedParticipation.getWarningProcessed()) {
            return; // 이미 처리된 경우 중복 처리 방지
        }
        // 팀원 수 조회
        long totalMembers = mentoringParticipationRepository.countByMentoringTeamIdAndParticipationStatusAndIsDeleted(teamId, MentoringParticipationStatus.ACCEPTED);

        // 과반수 이상의 신고 횟수인지 확인
        if (reportedParticipation.getReportingCnt() >= Math.ceil(totalMembers / 2.0)) {
            // 경고 횟수 증가
            User reportedUser = reportedParticipation.getUser();
            reportedUser.incrementWarningCnt();

            // 경고 처리 상태 업데이트
            reportedParticipation.setWarningProcessed(true);
        }
    }
    private User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUserDto securityUser = (SecurityUserDto) authentication.getPrincipal();
        Long userId = securityUser.getUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        return user;
    }
}
