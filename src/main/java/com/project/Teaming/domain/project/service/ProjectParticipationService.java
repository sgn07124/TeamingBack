package com.project.Teaming.domain.project.service;

import com.project.Teaming.domain.project.dto.request.JoinTeamDto;
import com.project.Teaming.domain.project.dto.response.ProjectParticipationInfoDto;
import com.project.Teaming.domain.project.dto.response.ProjectTeamMemberDto;
import com.project.Teaming.domain.project.entity.ParticipationStatus;
import com.project.Teaming.domain.project.entity.ProjectParticipation;
import com.project.Teaming.domain.project.entity.ProjectRole;
import com.project.Teaming.domain.project.entity.ProjectTeam;
import com.project.Teaming.domain.project.repository.ProjectParticipationRepository;
import com.project.Teaming.domain.project.repository.ProjectTeamRepository;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.repository.ReportRepository;
import com.project.Teaming.domain.user.repository.ReviewRepository;
import com.project.Teaming.domain.user.repository.UserRepository;
import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.exception.BusinessException;
import com.project.Teaming.global.jwt.dto.SecurityUserDto;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
        User user = getLoginUser();
        ProjectParticipation projectParticipation = ProjectParticipation.create(user, projectTeam);
        projectParticipationRepository.save(projectParticipation);
    }

    private User getLoginUser() {
        return userRepository.findById(getCurrentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));
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
            // 팀장이 탈퇴 시
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
                projectParticipation.updateOwnerRole();
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

        // 로그인 사용자의 참여 정보 찾기
        ProjectParticipation loginUserParticipation = teamMembers.stream()
                .filter(member -> member.getUser().getId().equals(user.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_PART_OF_TEAM));

        // 로그인한 사용자가 탈퇴 또는 강퇴된 경우 예외 처리 >> 해당 유저 입장에서는 팀원이 아님
        if (loginUserParticipation.getIsDeleted()) {
            throw new BusinessException(ErrorCode.USER_NOT_PART_OF_TEAM);
        }

        // 팀원 ID 목록
        List<Long> teamMemberIds = teamMembers.stream()
                .map(member -> member.getUser().getId())
                .toList();

        // 신고 정보와 리뷰 정보를 한 번의 쿼리로 가져옴
        Set<Long> reportedUserIds = reportRepository.findAllByProjectParticipationAndReportedUserIn(loginUserParticipation, teamMemberIds);
        Set<Long> reviewedUserIds = reviewRepository.findAllByProjectParticipationAndRevieweeIn(loginUserParticipation, teamMemberIds);

        return teamMembers.stream()
                .map(member -> {
                    ProjectTeamMemberDto dto = new ProjectTeamMemberDto(member);
                    dto.setLoginUser(member.getUser().getId().equals(user.getId()));
                    dto.setReported(reportedUserIds.contains(member.getUser().getId()));
                    dto.setReviewed(reviewedUserIds.contains(member.getUser().getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
