package com.project.Teaming.domain.project.service;

import com.project.Teaming.domain.project.dto.request.JoinTeamDto;
import com.project.Teaming.domain.project.dto.request.ReviewDto;
import com.project.Teaming.domain.project.dto.response.ProjectParticipationInfoDto;
import com.project.Teaming.domain.project.dto.response.ProjectTeamMemberDto;
import com.project.Teaming.domain.project.entity.ParticipationStatus;
import com.project.Teaming.domain.project.entity.ProjectParticipation;
import com.project.Teaming.domain.project.entity.ProjectRole;
import com.project.Teaming.domain.project.entity.ProjectStatus;
import com.project.Teaming.domain.project.entity.ProjectTeam;
import com.project.Teaming.domain.project.repository.ProjectParticipationRepository;
import com.project.Teaming.domain.project.repository.ProjectTeamRepository;
import com.project.Teaming.domain.user.entity.Report;
import com.project.Teaming.domain.user.entity.Review;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.repository.ReportRepository;
import com.project.Teaming.domain.user.repository.ReviewRepository;
import com.project.Teaming.domain.user.repository.UserRepository;
import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.exception.BusinessException;
import com.project.Teaming.global.jwt.dto.SecurityUserDto;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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
public class ProjectParticipationService {

    private final ProjectParticipationRepository projectParticipationRepository;
    private final ProjectTeamRepository projectTeamRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final ReviewRepository reviewRepository;

    public void createParticipation(ProjectTeam projectTeam) {
        ProjectParticipation projectParticipation = new ProjectParticipation();
        User user = userRepository.findById(getCurrentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        projectParticipation.createProjectParticipation(user, projectTeam);
        projectParticipationRepository.save(projectParticipation);
    }

    private Long getCurrentId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUserDto securityUser = (SecurityUserDto) authentication.getPrincipal();
        return securityUser.getUserId();
    }

    public void joinTeam(JoinTeamDto dto) {
        ProjectTeam projectTeam = projectTeamRepository.findById(dto.getTeamId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROJECT_TEAM));

        User user = userRepository.findById(getCurrentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        // 이미 팀에 참여했는지 여부 확인
        Optional<ProjectParticipation> existingParticipation = projectParticipationRepository.findByProjectTeamIdAndUserId(dto.getTeamId(), user.getId());
        if (existingParticipation.isPresent()) {
            ProjectParticipation participation = existingParticipation.get();
            if (participation.getRole() == ProjectRole.OWNER) {
                throw new BusinessException(ErrorCode.ALREADY_PARTICIPATED_OWNER);
            } else if (participation.getRole() == ProjectRole.MEMBER) {
                throw new BusinessException(ErrorCode.ALREADY_PARTICIPATED_MEMBER);
            }
        }

        // 새로운 팀에 참여
        ProjectParticipation newParticipation = new ProjectParticipation();
        newParticipation.joinTeamMember(user, projectTeam, dto.getRecruitCategory());
        projectParticipationRepository.save(newParticipation);
    }

    public void cancelTeam(Long teamId) {
        User user = userRepository.findById(getCurrentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        ProjectParticipation participation = projectParticipationRepository.findByProjectTeamIdAndUserId(teamId, user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROJECT_PARTICIPATION));

        if (participation.getParticipationStatus() == ParticipationStatus.PENDING && !participation.getIsDeleted()) {
            projectParticipationRepository.delete(participation);
        } else {
            throw new BusinessException(ErrorCode.INVALID_PARTICIPATION_ERROR);
        }
    }

    public void quitTeam(Long teamId) {
        User user = userRepository.findById(getCurrentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        ProjectParticipation projectParticipation = projectParticipationRepository.findByProjectTeamIdAndUserId(teamId, user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROJECT_PARTICIPATION));

        if (projectParticipation.canQuit()) {
            if (projectParticipation.getRole().equals(ProjectRole.OWNER)) {
                Optional<ProjectParticipation> firstMember = projectParticipationRepository.findTeamUsers(teamId, ParticipationStatus.ACCEPTED, ProjectRole.MEMBER)
                        .stream().findFirst();
                firstMember.ifPresentOrElse(
                        participation -> {
                            participation.setRole(ProjectRole.OWNER);
                        },
                        () -> {
                            throw new BusinessException(ErrorCode.NO_ELIGIBLE_MEMBER_FOR_LEADER);
                        }
                );
            }
            projectParticipation.quitTeam();
        } else {
            throw new BusinessException(ErrorCode.CANNOT_QUIT_TEAM);
        }
    }

    public void acceptedMember(Long teamId, Long userId) {
        User user = userRepository.findById(getCurrentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));
        ProjectParticipation joinMember = projectParticipationRepository.findByProjectTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROJECT_PARTICIPATION));
        ProjectParticipation teamOwner = projectParticipationRepository.findByProjectTeamIdAndRole(teamId, ProjectRole.OWNER)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROJECT_OWNER));

        if (joinMember.canAccept() && isTeamOwner(user, teamOwner)) {
            joinMember.acceptTeam();
        } else {
            throw new BusinessException(ErrorCode.CANNOT_ACCEPT_MEMBER);
        }
    }

    public void rejectedMember(Long teamId, Long userId) {
        User user = userRepository.findById(getCurrentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));
        ProjectParticipation joinMember = projectParticipationRepository.findByProjectTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROJECT_PARTICIPATION));
        ProjectParticipation teamOwner = projectParticipationRepository.findByProjectTeamIdAndRole(teamId, ProjectRole.OWNER)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROJECT_OWNER));

        if (joinMember.canReject() && isTeamOwner(user, teamOwner)) {
            joinMember.rejectTeam();
        } else {
            throw new BusinessException(ErrorCode.CANNOT_REJECT_MEMBER);
        }
    }

    public List<ProjectParticipationInfoDto> getAllParticipationDtos(Long teamId) {
        return projectParticipationRepository.findByProjectTeamId(teamId).stream()
                .map(ProjectParticipationInfoDto::new)
                .collect(Collectors.toList());
    }

    public void exportMember(Long teamId, Long userId) {
        User user = userRepository.findById(getCurrentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));
        ProjectParticipation exportMember = projectParticipationRepository.findByProjectTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROJECT_PARTICIPATION));
        ProjectParticipation teamOwner = projectParticipationRepository.findByProjectTeamIdAndRole(teamId, ProjectRole.OWNER)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROJECT_OWNER));

        if (isTeamOwner(user, teamOwner)) {
            exportMember.exportTeam();
        } else {
            throw new BusinessException(ErrorCode.FAIL_TO_EXPORT_TEAM);
        }
    }

    private static boolean isTeamOwner(User user, ProjectParticipation teamOwner) {
        return user.getId().equals(teamOwner.getUser().getId());
    }

    public List<ProjectTeamMemberDto> getAllMembers(Long teamId) {
        User user = userRepository.findById(getCurrentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        // 현재 팀원 목록 조회
        List<ProjectParticipation> teamMembers = projectParticipationRepository.findByProjectTeamIdAndParticipationStatus(teamId, ParticipationStatus.ACCEPTED);

        // 팀의 멤버인지 판별
        boolean isMember = projectParticipationRepository.existsByProjectTeamIdAndUserIdAndParticipationStatusAndIsDeleted(teamId, user.getId(),
                ParticipationStatus.ACCEPTED, false);
        if (!isMember) {
            throw new BusinessException(ErrorCode.USER_NOT_PART_OF_TEAM);
        }

        // 로그인 사용자가 팀원인지 판별
        ProjectParticipation loginUserParticipation = projectParticipationRepository
                .findByProjectTeamIdAndUserIdAndParticipationStatus(teamId, user.getId(), ParticipationStatus.ACCEPTED)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_PART_OF_TEAM));

        return teamMembers.stream()
                .map(member -> {
                    ProjectTeamMemberDto dto = new ProjectTeamMemberDto(member);
                    dto.setLoginUser(member.getUser().getId().equals(user.getId()));  // 로그인 한 유저인지
                    boolean isReported = reportRepository.existsByProjectParticipationAndReportedUser(loginUserParticipation, member.getUser());
                    dto.setReported(isReported);
                    boolean isReviewed = reviewRepository.existsByProjectParticipationAndReviewee(loginUserParticipation, member.getUser());
                    dto.setReviewed(isReviewed);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public void reportUser(Long teamId, Long reportedUserId) {
        // 로그인 사용자 조회(신고자)
        User reporter = userRepository.findById(getCurrentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        // 신고자의 참여 정보 조회
        ProjectParticipation reporterParticipation = projectParticipationRepository.findByProjectTeamIdAndUserId(teamId, reporter.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROJECT_PARTICIPATION));

        // 신고 대상의 참여 정보 조회
        ProjectParticipation reportedParticipation = projectParticipationRepository.findByProjectTeamIdAndUserId(
                        reporterParticipation.getProjectTeam().getId(), reportedUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REPORT_TARGET));

        // 본인에 대한 신고 불가
        if (reporter.getId().equals(reportedUserId)) {
            throw new BusinessException(ErrorCode.INVALID_SELF_ACTION);
        }
        Report report = Report.projectReport(reporterParticipation, reportedParticipation.getUser());
        reportRepository.save(report);

        // 받은 신고 과반수 체크 및 warningCnt 증가
        updateReportedWarningCount(reportedParticipation);
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

            // 처리된 신고에 대해 warningProcess = true 설정
            reportRepository.updateWarningProcessedByReportedUserAndTeamId(reportedUserId, teamId);
        }
    }

    public void reviewUser(ReviewDto dto) {
        // 로그인 사용자 조회(리뷰 작성자)
        User reviewer = userRepository.findById(getCurrentId()).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        // 리뷰 작성자의 참여 정보 조회
        ProjectParticipation reviewerParticipation = projectParticipationRepository.findByProjectTeamIdAndUserId(dto.getTeamId(), reviewer.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROJECT_PARTICIPATION));

        // 리뷰 대상의 참여 정보 조회
        ProjectParticipation revieweeParticipation = projectParticipationRepository.findByProjectTeamIdAndUserId(
                        reviewerParticipation.getProjectTeam().getId(), dto.getRevieweeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REVIEW_TARGET));

        // 프로젝트가 완료된 경우에만 리뷰 가능
        if (revieweeParticipation.getProjectTeam().getStatus().equals(ProjectStatus.COMPLETE)) {
            // 본인에 대한 리뷰는 작성 불가
            if (reviewer.getId().equals(revieweeParticipation.getUser().getId())) {
                throw new BusinessException(ErrorCode.INVALID_SELF_ACTION);
            }
            Review review = Review.projectReview(reviewerParticipation, revieweeParticipation.getUser(), dto.getRating(), dto.getContent());
            reviewRepository.save(review);
        } else {
            throw new BusinessException(ErrorCode.PROJECT_NOT_COMPLETE);
        }
    }
}
