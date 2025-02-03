package com.project.Teaming.domain.mentoring.service;

import com.project.Teaming.domain.mentoring.dto.response.ForLeaderResponse;
import com.project.Teaming.domain.mentoring.dto.response.ParticipationForUserResponse;
import com.project.Teaming.domain.mentoring.dto.response.TeamParticipationResponse;
import com.project.Teaming.domain.mentoring.dto.response.TeamUserResponse;
import com.project.Teaming.domain.mentoring.entity.MentoringParticipationStatus;
import com.project.Teaming.domain.mentoring.entity.MentoringStatus;
import com.project.Teaming.domain.mentoring.entity.MentoringTeam;
import com.project.Teaming.domain.mentoring.repository.MentoringParticipationRepository;
import com.project.Teaming.domain.user.service.ReportService;
import com.project.Teaming.domain.user.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class AsyncParticipationService {

    private final MentoringParticipationRepository mentoringParticipationRepository;
    private final RedisTeamUserManagementService redisTeamUserManagementService;
    private final RedisApplicantManagementService redisApplicantManagementService;
    private final ReviewService reviewService;
    private final ReportService reportService;

    @Async
    @Transactional
    public CompletableFuture<List<TeamUserResponse>> fetchAndSetTeamUsers(MentoringTeam mentoringTeam, Long userId, Long participationId) {
        List<TeamUserResponse> teamUsers = mentoringParticipationRepository.findAllByMemberStatus(
                mentoringTeam, MentoringStatus.COMPLETE, MentoringParticipationStatus.ACCEPTED, participationId);
        setLoginStatus(teamUsers, userId); // 로그인 상태 설정
        return CompletableFuture.completedFuture(teamUsers);
    }

    @Async
    @Transactional
    public CompletableFuture<List<TeamUserResponse>> fetchAndValidateDeletedOrExportedUsers(Long teamId, Long participationId) {
        List<TeamUserResponse> deletedOrExportedUsers = redisTeamUserManagementService.getDeletedOrExportedParticipations(teamId);
        if (deletedOrExportedUsers != null && !deletedOrExportedUsers.isEmpty()) {
            reviewService.setReviewInfo(deletedOrExportedUsers, participationId); // 리뷰 여부 검증
            reportService.setReportInfo(deletedOrExportedUsers, participationId); // 신고 여부 검증
        }
        return CompletableFuture.completedFuture(deletedOrExportedUsers);
    }

    @Async
    @Transactional
    public CompletableFuture<List<TeamParticipationResponse>> fetchApplicantsAsync(Long teamId) {
        List<TeamParticipationResponse> applicants = redisApplicantManagementService.getApplicants(teamId);
        return CompletableFuture.completedFuture(applicants);
    }

    @Async
    @Transactional
    public CompletableFuture<ForLeaderResponse> createForLeaderResponseAsync(
            List<TeamUserResponse> combineUsers, CompletableFuture<List<TeamParticipationResponse>> applicantsFuture) {

        //dto 먼저 생성
        ForLeaderResponse dto = new ForLeaderResponse();
        dto.setMembers(combineUsers);

        // 지원자 데이터 비동기 작업 결과 set
        List<TeamParticipationResponse> applicants = applicantsFuture.join();
        dto.setParticipations(applicants);

        return CompletableFuture.completedFuture(dto);
    }

    /**
     * 로그인 한 사용자 있는지 확인하는 로직
     * @param dtos
     * @param userId
     */
    private void setLoginStatus(List<?> dtos, Long userId) {
        dtos.forEach(dto -> {
            if (dto instanceof TeamUserResponse teamDto && teamDto.getUserId().equals(userId)) {
                teamDto.setIsLogined(true);
            } else if (dto instanceof ParticipationForUserResponse userDto && userDto.getUserId().equals(userId)) {
                userDto.setIsLogined(true);
            }
        });
    }
}
