package com.project.Teaming.domain.user.service;

import com.project.Teaming.domain.mentoring.dto.response.TeamUserResponse;
import com.project.Teaming.domain.user.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;

    /**
     * 사용자에 대한 신고 상태를 설정합니다.
     */
    public void setReportInfo(List<TeamUserResponse> users, Long currentParticipationId) {
        // 사용자 ID 목록 추출
        Set<Long> userIds = users.stream()
                .map(TeamUserResponse::getUserId)
                .collect(Collectors.toSet());

        // 신고 상태 조회
        Set<Long> reportedUserIds = reportRepository.findReportedUserIds(currentParticipationId, userIds);
        // 신고 상태 설정
        users.forEach(user -> user.setIsReported(reportedUserIds.contains(user.getUserId())));
    }
}
