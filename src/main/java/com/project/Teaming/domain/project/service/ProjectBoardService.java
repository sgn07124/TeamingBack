package com.project.Teaming.domain.project.service;

import com.project.Teaming.domain.project.dto.request.CreatePostDto;
import com.project.Teaming.domain.project.dto.response.ProjectPostInfoDto;
import com.project.Teaming.domain.project.dto.response.ProjectPostListDto;
import com.project.Teaming.domain.project.dto.response.ProjectPostStatusDto;
import com.project.Teaming.domain.project.entity.ParticipationStatus;
import com.project.Teaming.domain.project.entity.PostStatus;
import com.project.Teaming.domain.project.entity.ProjectBoard;
import com.project.Teaming.domain.project.entity.ProjectTeam;
import com.project.Teaming.domain.project.repository.ProjectBoardRepository;
import com.project.Teaming.domain.project.repository.ProjectParticipationRepository;
import com.project.Teaming.domain.project.repository.ProjectTeamRepository;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.repository.UserRepository;
import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.exception.BusinessException;
import com.project.Teaming.global.jwt.dto.SecurityUserDto;
import com.project.Teaming.global.result.pagenateResponse.PaginatedCursorResponse;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProjectBoardService {

    private final ProjectBoardRepository projectBoardRepository;
    private final ProjectTeamRepository projectTeamRepository;
    private final UserRepository userRepository;
    private final ProjectParticipationRepository projectParticipationRepository;
    private final ProjectCacheService projectCacheService;

    /**
     * 프로젝트 팀 게시물 작성
     */
    public void createPost(Long teamId, CreatePostDto createPostDto) {
        validateTeamMember(teamId);
        ProjectTeam projectTeam = getProjectTeam(teamId);
        ProjectBoard post = ProjectBoard.projectBoard(createPostDto, projectTeam);
        projectBoardRepository.save(post);
        // 게시글 추가 후 최신 게시글 반영을 위한 캐시 무효화. (첫 페이지 캐시 삭제)
        projectCacheService.evictCache(null, 10);
    }

    /**
     * 프로젝트 팀 게시물 상세 조회
     */
    public ProjectPostInfoDto getPostInfo(Long postId) {
        ProjectBoard projectBoard = getProjectBoard(postId);
        ProjectTeam projectTeam = getProjectTeam(projectBoard.getProjectTeam().getId());

        List<String> stackIds = extractStackIds(projectTeam);
        List<String> recruitCategoryIds = extractRecruitCategoryIds(projectTeam);

        boolean isMember = isProjectMember(projectTeam.getId());
        boolean isApply = isProjectApply(projectTeam.getId());
        return ProjectPostInfoDto.from(projectTeam, projectBoard, stackIds, recruitCategoryIds, isMember, isApply);
    }

    private boolean isProjectApply(Long projectTeamId) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return false;  // 인증되지 않은 사용자(일반 사용자)
        }
        return isApply(user.getId(), projectTeamId);
    }

    private boolean isApply(Long userId, Long projectTeamId) {
        return projectParticipationRepository.existsByProjectTeamIdAndUserIdAndDecisionDateIsNull(projectTeamId, userId);
    }

    private static List<String> extractStackIds(ProjectTeam projectTeam) {
        return projectTeam.getStacks().stream()
                .map(teamStack -> String.valueOf(teamStack.getStack().getId()))
                .collect(Collectors.toList());
    }

    private static List<String> extractRecruitCategoryIds(ProjectTeam projectTeam) {
        return projectTeam.getRecruitCategories().stream()
                .map(teamRecruitCategory -> String.valueOf(teamRecruitCategory.getRecruitCategory().getId()))
                .collect(Collectors.toList());
    }

    /**
     * 프로젝트 팀의 멤버인지 확인(인증이 안된 유저, 인증이 됬지만 팀원이 아닌 유저, 인증이 됬으며 팀원인 유저 중 판별)
     */
    public boolean isProjectMember(Long projectTeamId) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return false;  // 인증되지 않은 사용자(일반 사용자)
        }
        return isMember(user.getId(), projectTeamId);  // 로그인 한 사용자들 중 팀원 여부
    }

    /**
     * 해당 메서드를 조회한 사용자의 토큰(인증) 유무를 확인
     */
    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            return userRepository.findById(getCurrentId()).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROJECT_TEAM));
        }
        return null;  // 인증되지 않은 사용자
    }

    private boolean isMember(Long userId, Long projectTeamId) {
        return projectParticipationRepository.existsByProjectTeamIdAndUserIdAndParticipationStatusAndIsDeleted(projectTeamId, userId,
                ParticipationStatus.ACCEPTED, false);
    }

    /**
     * 프로젝트 팀 게시물 수정
     */
    public void updatePost(Long teamId, Long postId, CreatePostDto dto) {
        validateTeamMember(teamId);
        ProjectTeam projectTeam = getProjectTeam(teamId);
        ProjectBoard projectBoard = getProjectBoard(postId);

        projectBoard.updateProjectBoard(dto, projectTeam);
    }

    /**
     * 프로젝트 팀 게시물 삭제
     */
    public void deletePost(Long teamId, Long postId) {
        validateTeamMember(teamId);
        ProjectBoard projectBoard = getProjectBoard(postId);
        projectBoardRepository.delete(projectBoard);
    }

    /**
     * 게시글 목록 조회
     */
    public PaginatedCursorResponse<ProjectPostListDto> getProjectPosts(Long lastCursor, int pageSize) {
        // Cursor 기준 게시글 가져오기
        Pageable pageable = PageRequest.of(0, pageSize + 1, Sort.by(Direction.DESC, "createdDate"));  // pageSize+1은 다음 페이지의 첫 값을 nextCursor로 설정하기 위해
        List<ProjectBoard> projectBoards = projectBoardRepository.findAllByCursor(lastCursor, pageable);

        List<ProjectPostListDto> content = projectBoards.stream()
                .distinct()  // 중복 제거
                .limit(pageSize)  // 요청한 페이지 크기로 제한
                .map(this::convertToProjectPostListDto)
                .toList();

        // Cursor 기반 페이징 응답
        boolean isLast = projectBoards.size() <= pageSize; // 반환된 데이터 수가 요청한 pageSize보다 적으면 마지막 페이지
        Long nextCursor = isLast ? null : projectBoards.get(pageSize).getId(); // 다음 커서 값 설정
        return new PaginatedCursorResponse<>(content, nextCursor, pageSize, isLast);
    }

    private ProjectPostListDto convertToProjectPostListDto(ProjectBoard projectBoard) {
        ProjectTeam projectTeam = projectBoard.getProjectTeam();
        List<String> stackIds = extractStackIds(projectTeam);
        return ProjectPostListDto.from(projectTeam, projectBoard, stackIds);
    }

    /**
     * 프로젝트 팀의 게시글 조회
     */
    public List<ProjectPostListDto> getTeamProjectPosts(Long teamId) {
        List<ProjectBoard> projectBoards = projectBoardRepository.findAllByProjectTeamId(teamId);
        return projectBoards.stream()
                .map(this::convertToProjectPostListDto)
                .toList();
    }

    /**
     * 게시글 상태 완료 처리
     */
    public ProjectPostStatusDto completePostStatus(Long teamId, Long postId) {
        validateTeamMember(teamId);

        ProjectBoard projectBoard = getProjectBoard(postId);
        projectBoard.updateStatus();
        return ProjectPostStatusDto.from(projectBoard);
    }

    @Scheduled(cron = "0 0 0 * * ?")  // 매일 자정에 실행
    public void updateCheckCompleteStatus() {
        List<ProjectBoard> posts = projectBoardRepository.findAllByStatus(PostStatus.RECRUITING);
        for (ProjectBoard post : posts) {
            post.checkDeadline();
            projectBoardRepository.save(post);
        }
    }

    // 팀원인지 검증하는 메서드
    private void validateTeamMember(Long teamId) {
        User user = getLoginUser();
        boolean isMember = isMember(user.getId(), teamId);
        if (!isMember) {
            throw new BusinessException(ErrorCode.USER_NOT_PART_OF_TEAM);
        }
    }

    private Long getCurrentId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUserDto securityUser = (SecurityUserDto) authentication.getPrincipal();
        return securityUser.getUserId();
    }

    private User getLoginUser() {
        return userRepository.findById(getCurrentId()).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));
    }

    private ProjectTeam getProjectTeam(Long teamId) {
        return projectTeamRepository.findById(teamId).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROJECT_TEAM));
    }

    private ProjectBoard getProjectBoard(Long postId) {
        return projectBoardRepository.findById(postId).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROJECT_POST));
    }
}
