package com.project.Teaming.domain.user.service;

import com.project.Teaming.domain.project.entity.ParticipationStatus;
import com.project.Teaming.domain.project.entity.ProjectParticipation;
import com.project.Teaming.domain.project.entity.ProjectRole;
import com.project.Teaming.domain.project.entity.ProjectTeam;
import com.project.Teaming.domain.project.entity.Stack;
import com.project.Teaming.domain.project.repository.ProjectBoardRepository;
import com.project.Teaming.domain.project.repository.ProjectParticipationRepository;
import com.project.Teaming.domain.project.repository.ProjectTeamRepository;
import com.project.Teaming.domain.user.dto.request.UpdateUserInfoDto;
import com.project.Teaming.domain.user.dto.response.ReviewDto;
import com.project.Teaming.domain.user.dto.response.UserInfoDto;
import com.project.Teaming.domain.user.dto.response.UserReportCnt;
import com.project.Teaming.domain.user.entity.UserStack;
import com.project.Teaming.domain.project.repository.StackRepository;
import com.project.Teaming.domain.user.dto.request.RegisterDto;
import com.project.Teaming.domain.user.entity.Portfolio;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.repository.PortfolioRepository;
import com.project.Teaming.domain.user.repository.ReportRepository;
import com.project.Teaming.domain.user.repository.ReviewRepository;
import com.project.Teaming.domain.user.repository.UserRepository;
import com.project.Teaming.domain.user.repository.UserStackRepository;
import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.exception.BusinessException;
import com.project.Teaming.global.jwt.dto.SecurityUserDto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final StackRepository stackRepository;
    private final UserStackRepository userStackRepository;
    private final ReviewRepository reviewRepository;
    private final ProjectTeamRepository projectTeamRepository;
    private final ProjectParticipationRepository projectParticipationRepository;
    private final ProjectBoardRepository projectBoardRepository;
    private final ReportRepository reportRepository;
    private final UserNotificationService userNotificationService;

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public void saveUser(String email, String provider, String role) {
        User user = new User(email, provider, role);
        userRepository.save(user);
    }

    @Transactional
    public void saveUserInfo(RegisterDto dto) {
        log.info("email : " + getSecurityUserDto().getEmail());
        User user = findByEmail(getSecurityUserDto().getEmail()).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST));

        user.updateUserInfo(dto.getName());  // 유저에 닉네임 저장
        Portfolio portfolio = new Portfolio();
        user.registerPortfolio(portfolio);
        if (dto.getIntroduce() != null && !dto.getIntroduce().trim().isEmpty()) {
            user.linkPortfolio(portfolio, dto);  // 포트폴리오에 자기소개 저장
        }
        if (dto.getStackIds() != null && !dto.getStackIds().isEmpty()) {
            saveUserStacks(dto, portfolio);  // 포트폴리오에 기술스택 저장
        }

        portfolioRepository.save(portfolio);
        userRepository.save(user);
        userNotificationService.join(user);
    }

    private void saveUserStacks(RegisterDto dto, Portfolio portfolio) {
        List<Long> stackIds = dto.getStackIds();
        List<Stack> stacks = stackRepository.findAllById(stackIds);

        // 누락된 기술 스택 ID 검증
        List<Long> missingStackIds = stackIds.stream()
                .filter(id -> stacks.stream().noneMatch(stack -> stack.getId().equals(id)))
                .collect(Collectors.toList());
        if (!missingStackIds.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_VALID_STACK_ID);
        }

        for (Stack stack : stacks) {
            UserStack userStack = UserStack.addStacks(portfolio, stack);
            userStackRepository.save(userStack);
        }
    }

    @Transactional
    public void updateUser(UpdateUserInfoDto dto) {
        User user = findByEmail(getSecurityUserDto().getEmail()).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST));
        Portfolio portfolio = portfolioRepository.findById(user.getPortfolio().getId()).orElseThrow(() -> new BusinessException(ErrorCode.PORTFOLIO_NOT_EXIST));

        user.updateUserInfo(dto.getName());
        portfolio.updatePortfolioInfo(dto.getIntroduce());

        List<Stack> stacks = stackRepository.findAllById(dto.getStackIds());
        List<Long> missingStackIds = dto.getStackIds().stream()
                .filter(id -> stacks.stream().noneMatch(stack -> stack.getId().equals(id)))
                .collect(Collectors.toList());
        if (!missingStackIds.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_VALID_STACK_ID);
        }

        portfolio.updateStacks(stacks);
    }

    public UserInfoDto getAuthenticatedUserInfo() {
        SecurityUserDto securityUserDto = getSecurityUserDto();
        return getUserInfo(securityUserDto.getUserId());
    }


    public UserInfoDto getUserInfo(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST));

        UserInfoDto dto = new UserInfoDto();
        Portfolio portfolio = portfolioRepository.findById(user.getPortfolio().getId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.PORTFOLIO_NOT_EXIST));

        // 기술 스택 id 리스트 생성
        List<String> stackIds = portfolio.getUserStacks().stream()
                .map(userStack -> String.valueOf(userStack.getStack().getId()))
                .collect(Collectors.toList());

        dto.setUserInfoDto(user, portfolio, stackIds);
        return dto;
    }

    public List<ReviewDto> getAuthenticatedUserReviews() {
        SecurityUserDto securityUserDto = getSecurityUserDto();
        return getReviews(securityUserDto.getUserId());
    }

    public List<ReviewDto> getReviews(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST));

        List<ReviewDto> projectReviewsByUser = reviewRepository.findProjectReviewsByUser(user);
        List<ReviewDto> mentoringReviewsByUser = reviewRepository.findMentoringReviewsByUser(user);

        List<ReviewDto> allReviews = new ArrayList<>();
        allReviews.addAll(projectReviewsByUser);
        allReviews.addAll(mentoringReviewsByUser);

        allReviews = allReviews.stream()
                .sorted(Comparator.comparing(ReviewDto::getCreatedDate))
                .collect(Collectors.toList());
        return allReviews;
    }

    public UserReportCnt getWarningCnt() {
        User user = findByEmail(getSecurityUserDto().getEmail()).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST));
        UserReportCnt userReportCnt = new UserReportCnt();
        userReportCnt.setReportCnt(user.getWarningCount());
        return userReportCnt;
    }

    private static SecurityUserDto getSecurityUserDto() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUserDto securityUser = (SecurityUserDto) authentication.getPrincipal();
        return securityUser;
    }

    @Transactional
    public void withdrawUser() {
        User user = findByEmail(getSecurityUserDto().getEmail()).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST));
        user.updateUserWithdraw();

        List<ProjectParticipation> projectParticipations = projectParticipationRepository.findByUser(user);
        quitProjectTeams(projectParticipations);
    }

    private void quitProjectTeams(List<ProjectParticipation> projectParticipations) {
        for (ProjectParticipation participation : projectParticipations) {
            ProjectTeam team = participation.getProjectTeam();

            if (participation.getRole().equals(ProjectRole.OWNER)) {
                Optional<ProjectParticipation> firstMember = projectParticipationRepository.findTeamUsers(team.getId(), ParticipationStatus.ACCEPTED, ProjectRole.MEMBER)
                        .stream().findFirst();
                firstMember.ifPresentOrElse(
                        nextLeader -> nextLeader.setRole(ProjectRole.OWNER),
                        () -> { // 팀장 받을 유저가 없는 경우 - 팀 삭제
                            // Report, Review 테이블에서 project_participation_id를 null로 설정
                            reportRepository.updateProjectParticipationNull(participation.getId());
                            reviewRepository.updateProjectParticipationNull(participation.getId());
                            // 팀원이 없다면 모든 참여 기록 삭제 후 팀 삭제
                            projectBoardRepository.deleteByProjectTeamId(team.getId());
                            projectParticipationRepository.deleteAllByProjectTeamId(team.getId());
                            projectTeamRepository.delete(team);
                        }
                );
            }
            reportRepository.updateProjectParticipationNull(participation.getId());
            reviewRepository.updateProjectParticipationNull(participation.getId());
            // 해당 팀 탈퇴
            projectParticipationRepository.delete(participation);
        }
    }
}
