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

    private Long getCurrentId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUserDto securityUser = (SecurityUserDto) authentication.getPrincipal();
        return securityUser.getUserId();
    }

    public void createPost(Long teamId, CreatePostDto createPostDto) {
        ProjectTeam projectTeam = projectTeamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROJECT_TEAM));

        User user = userRepository.findById(getCurrentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        boolean isMember = projectParticipationRepository.existsByProjectTeamIdAndUserIdAndParticipationStatusAndIsDeleted(teamId, user.getId(),
                ParticipationStatus.ACCEPTED, false);
        if (!isMember) {
            throw new BusinessException(ErrorCode.USER_NOT_PART_OF_TEAM);
        }
        ProjectBoard post = ProjectBoard.projectBoard(createPostDto, projectTeam);

        projectBoardRepository.save(post);
    }

    public ProjectPostInfoDto getPostInfo(Long postId) {
        ProjectBoard projectBoard = projectBoardRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROJECT_POST));

        ProjectTeam projectTeam = projectTeamRepository.findById(projectBoard.getProjectTeam().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROJECT_TEAM));

        List<String> stackIds = projectTeam.getStacks().stream()
                .map(teamStack -> String.valueOf(teamStack.getStack().getId()))
                .collect(Collectors.toList());

        List<String> recruitCategoryIds = projectTeam.getRecruitCategories().stream()
                .map(teamRecruitCategory -> String.valueOf(teamRecruitCategory.getRecruitCategory().getId()))
                .collect(Collectors.toList());

        return ProjectPostInfoDto.from(projectTeam, projectBoard, stackIds, recruitCategoryIds);
    }

    public void updatePost(Long teamId, Long postId, CreatePostDto dto) {
        ProjectTeam projectTeam = projectTeamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROJECT_TEAM));

        ProjectBoard projectBoard = projectBoardRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROJECT_POST));

        User user = userRepository.findById(getCurrentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        boolean isMember = projectParticipationRepository.existsByProjectTeamIdAndUserIdAndParticipationStatusAndIsDeleted(teamId, user.getId(),
                ParticipationStatus.ACCEPTED, false);
        if (!isMember) {
            throw new BusinessException(ErrorCode.USER_NOT_PART_OF_TEAM);
        }
        projectBoard.updateProjectBoard(dto, projectTeam);
    }

    public void deletePost(Long teamId, Long postId) {
        ProjectBoard projectBoard = projectBoardRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROJECT_POST));

        User user = userRepository.findById(getCurrentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        boolean isMember = projectParticipationRepository.existsByProjectTeamIdAndUserIdAndParticipationStatusAndIsDeleted(teamId, user.getId(),
                ParticipationStatus.ACCEPTED, false);
        if (!isMember) {
            throw new BusinessException(ErrorCode.USER_NOT_PART_OF_TEAM);
        }
        projectBoardRepository.delete(projectBoard);
    }

    public PaginatedCursorResponse<ProjectPostListDto> getProjectPosts(Long lastCursor, int pageSize) {
        // Cursor 기준 게시글 가져오기
        Pageable pageable = PageRequest.of(0, pageSize + 1, Sort.by(Direction.DESC, "createdDate"));  // pageSize+1은 다음 페이지의 첫 값을 nextCursor로 설정하기 위해
        List<ProjectBoard> projectBoards = projectBoardRepository.findAllByCursor(lastCursor, pageable);

        List<ProjectPostListDto> content = projectBoards.stream()
                .distinct()  // 중복 제거
                .limit(pageSize)  // 요청한 페이지 크기로 제한
                .map(projectBoard -> {
                    ProjectTeam projectTeam = projectBoard.getProjectTeam();
                    List<String> stackIds = projectTeam.getStacks().stream()
                            .map(stack -> String.valueOf(stack.getId()))
                            .toList();
                    return ProjectPostListDto.from(projectTeam, projectBoard, stackIds);
                }).toList();

        // Cursor 기반 페이징 응답
        boolean isLast = projectBoards.size() <= pageSize; // 반환된 데이터 수가 요청한 pageSize보다 적으면 마지막 페이지
        Long nextCursor = isLast ? null : projectBoards.get(pageSize).getId(); // 다음 커서 값 설정
        return new PaginatedCursorResponse<>(
                content,
                nextCursor, // 다음 커서 값 반환
                pageSize,
                isLast  // 마지막 페이지 여부
        );
    }

    public List<ProjectPostListDto> getTeamProjectPosts(Long teamId) {

        List<ProjectBoard> projectBoards = projectBoardRepository.findAllByProjectTeamId(teamId);

        return projectBoards.stream()
                .map(projectBoard -> {
                    ProjectTeam projectTeam = projectBoard.getProjectTeam();
                    List<String> stackIds = projectTeam.getStacks().stream()
                            .map(stack -> String.valueOf(stack.getId()))
                            .toList();
                    return ProjectPostListDto.from(projectTeam, projectBoard, stackIds);
                }).toList();
    }

    public ProjectPostStatusDto completePostStatus(Long teamId, Long postId) {
        User user = userRepository.findById(getCurrentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        boolean isMember = projectParticipationRepository.existsByProjectTeamIdAndUserIdAndParticipationStatusAndIsDeleted(teamId, user.getId(),
                ParticipationStatus.ACCEPTED, false);
        if (!isMember) {
            throw new BusinessException(ErrorCode.USER_NOT_PART_OF_TEAM);
        }

        ProjectBoard projectBoard = projectBoardRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROJECT_POST));
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
}
