package com.project.Teaming.domain.mentoring.service;

import com.project.Teaming.domain.mentoring.dto.request.BoardRequest;
import com.project.Teaming.domain.mentoring.dto.response.MentoringPostStatusResponse;
import com.project.Teaming.domain.mentoring.dto.response.BoardResponse;
import com.project.Teaming.domain.mentoring.dto.response.BoardSpecResponse;
import com.project.Teaming.domain.mentoring.entity.*;
import com.project.Teaming.domain.mentoring.provider.*;
import com.project.Teaming.domain.mentoring.repository.CategoryRepository;
import com.project.Teaming.domain.mentoring.repository.MentoringBoardRepository;
import com.project.Teaming.domain.mentoring.repository.MentoringParticipationRepository;
import com.project.Teaming.domain.mentoring.repository.MentoringTeamRepository;
import com.project.Teaming.domain.mentoring.service.policy.MentoringBoardPolicy;
import com.project.Teaming.domain.mentoring.service.policy.MentoringParticipationPolicy;
import com.project.Teaming.domain.mentoring.service.policy.MentoringTeamPolicy;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.repository.UserRepository;
import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.exception.BusinessException;
import com.project.Teaming.global.error.exception.MentoringPostNotFoundException;
import com.project.Teaming.global.error.exception.MentoringTeamNotFoundException;
import com.project.Teaming.global.error.exception.NoAuthorityException;
import com.project.Teaming.global.jwt.dto.SecurityUserDto;
import com.project.Teaming.global.result.pagenateResponse.PaginatedCursorResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MentoringBoardService {

    @PersistenceContext
    EntityManager entityManager;

    private final MentoringBoardRepository mentoringBoardRepository;
    private final MentoringBoardDataProvider mentoringBoardDataProvider;
    private final MentoringBoardPolicy mentoringBoardPolicy;
    private final MentoringTeamDataProvider mentoringTeamDataProvider;
    private final MentoringTeamPolicy mentoringTeamPolicy;
    private final UserDataProvider userDataProvider;
    private final MentoringParticipationRepository mentoringParticipationRepository;
    private final MentoringParticipationPolicy mentoringParticipationPolicy;
    private final CategoryRepository categoryRepository;
    private final CategoryDataProvider categoryDataProvider;

    /**
     * 게시물을 저장하는 로직
     * 저장된 post Id 반환
     * @param teamId
     * @param boardDto
     */
    @Transactional
    public Long saveMentoringPost(Long teamId, BoardRequest boardDto) {
        User user = userDataProvider.getUser();
        MentoringTeam mentoringTeam = mentoringTeamDataProvider.findMentoringTeam(teamId);

        mentoringParticipationPolicy.validateParticipation(
                mentoringTeam, user,null, MentoringParticipationStatus.ACCEPTED,
                null,false,() -> new BusinessException(ErrorCode.NO_AUTHORITY));

        MentoringBoard mentoringBoard = MentoringBoard.from(boardDto);
        mentoringBoard.link(Optional.ofNullable(boardDto.getLink())
                .orElse(mentoringTeam.getLink()));
        mentoringBoard.addMentoringBoard(mentoringTeam);  // 멘토링 팀과 연관관계 매핑

        MentoringBoard savedPost = mentoringBoardRepository.save(mentoringBoard);
        return savedPost.getId();
    }

    /**
     * 특정 게시물을 조회하는 로직
     * @param postId
     * @return
     */
    @Transactional(readOnly = true)
    public MentoringBoard findMentoringPost(Long postId) {

        MentoringBoard mentoringBoard = mentoringBoardDataProvider.findBoard(postId);

        return mentoringBoard;
    }

    @Transactional(readOnly = true)
    public BoardSpecResponse toDto(MentoringBoard mentoringPost) {
        User currentUser = userDataProvider.getOptionalUser();
        MentoringTeam mentoringTeam = mentoringPost.getMentoringTeam();
        BoardSpecResponse dto = mentoringPost.toDto(mentoringTeam);
        List<String> teamCategories = categoryRepository.findCategoryIdsByTeamId(mentoringTeam.getId());
        dto.setCategory(teamCategories);
        // 로그인된 사용자가 있는 경우 권한 설정
        if (currentUser != null) {
            setAuthorityForUser(dto, mentoringTeam, currentUser);
        } else {
            // 로그인되지 않은 경우 기본 권한 설정
            dto.setAuthority(MentoringAuthority.NoAuth);
            dto.setIsParticipate(false);
        }

        return dto;
    }

    /**
     * 특정 멘토링 팀의 삭제되지않은 모든 게시물들을 가져오는 로직
     * @param teamId
     * @return
     */
    @Transactional(readOnly = true)
    public List<BoardResponse> findAllMyMentoringPost(Long teamId) {
        MentoringTeam mentoringTeam = mentoringTeamDataProvider.findMentoringTeam(teamId);
        mentoringTeamPolicy.validateTeamStatus(mentoringTeam);

        List<BoardResponse> boards = mentoringBoardRepository.findAllByMentoringTeamId(teamId);
        List<String> categories = categoryRepository.findCategoryIdsByTeamId(teamId);
        boards.forEach(post -> {
            post.setCategory(categories);
        });
        return boards;
    }

    /**
     * 삭제되지 않은 모든 게시물들을 가져오는 로직
     *
     * @return
*/
    @Transactional(readOnly = true)
    public PaginatedCursorResponse<BoardResponse> findAllPosts(Long cursor, int size) {
        List<Long> ids = mentoringBoardRepository.findMentoringBoardIds(cursor, size + 1, Status.FALSE);

        // 2. 다음 페이지 여부 확인
        boolean isLast = ids.size() <= size;
        if (!isLast) {
            ids = ids.subList(0, size); // 추가된 데이터 제외하고 요청 크기만큼 자르기
        }

        List<MentoringBoard> boards = mentoringBoardRepository.findAllByIds(ids);

        List<Long> teamIds = boards.stream()
                .map(board -> board.getMentoringTeam().getId())
                .distinct()
                .toList();

        Map<Long, List<String>> categoryMap = categoryDataProvider.mapCategoriesByTeamIds(teamIds);


        List<BoardResponse> boardDtos = boards.stream()
                .map(board -> {
                    MentoringTeam mentoringTeam = board.getMentoringTeam();
                    List<String> categories = categoryMap.getOrDefault(mentoringTeam.getId(), List.of());
                    return BoardResponse.from(board, mentoringTeam, categories);
                })
                .toList();

        //  다음 커서 설정
        Long nextCursor = (boards.isEmpty() || isLast) ? null : boards.get(boards.size() - 1).getId();


        return new PaginatedCursorResponse<>(
                boardDtos,
                nextCursor, // 다음 커서 값 반환
                size,
                isLast // 마지막 페이지 여부
        );

    }

    /**
     *  게시물을 수정하는 로직
     * @param postId
     * @param dto
     * @return
     */
    @Transactional
    public void updateMentoringPost(Long postId, BoardRequest dto) {
        User user = userDataProvider.getUser();
        MentoringBoard mentoringBoard = mentoringBoardDataProvider.findBoard(postId);
        MentoringTeam mentoringTeam = mentoringBoard.getMentoringTeam();

        mentoringParticipationPolicy.validateParticipation(
                mentoringTeam, user,null, MentoringParticipationStatus.ACCEPTED,
                null,false,() -> new BusinessException(ErrorCode.NO_AUTHORITY));

        mentoringBoard.updateBoard(dto);
    }

    /**
     * 게시물을 삭제하는 로직
     * @param postId
     */
    @Transactional
    public void deleteMentoringPost(Long postId) {
        User user = userDataProvider.getUser();
        MentoringBoard mentoringBoard = mentoringBoardDataProvider.findBoard(postId);

        mentoringParticipationPolicy.validateParticipation(
                mentoringBoard.getMentoringTeam(), user, null,MentoringParticipationStatus.ACCEPTED,
                null,false,() -> new BusinessException(ErrorCode.NO_AUTHORITY));

        mentoringBoardRepository.delete(mentoringBoard);

    }

    /**
     * 팀원이 글에서 모집 현황을 수정할 수 있는 로직
     * @param teamId
     * @param postId
     * @return
     */
    @Transactional
    public MentoringPostStatusResponse updatePostStatus(Long teamId, Long postId) {
        User user = userDataProvider.getUser();
        MentoringTeam mentoringTeam = mentoringTeamDataProvider.findMentoringTeam(teamId);

        mentoringParticipationPolicy.validateParticipation(
                mentoringTeam, user,null, MentoringParticipationStatus.ACCEPTED,
                null, false,() -> new BusinessException(ErrorCode.NO_AUTHORITY));

        MentoringBoard post = mentoringBoardDataProvider.findBoard(postId);
        mentoringBoardPolicy.validatePostWithTeam(post,mentoringTeam);

        post.updateStatus();
        return new MentoringPostStatusResponse(PostStatus.COMPLETE);

    }

    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정 실행
    @Transactional
    public void updateCheckCompleteStatus() {
        mentoringBoardRepository.bulkUpDateStatus(PostStatus.COMPLETE, PostStatus.RECRUITING, LocalDate.now());
        entityManager.clear();
    }

    private void setAuthorityForUser(BoardSpecResponse dto, MentoringTeam mentoringTeam, User user) {
        Optional<MentoringParticipation> participations = mentoringParticipationRepository.findDynamicMentoringParticipation(
                mentoringTeam,
                user,null,null,
                List.of(MentoringParticipationStatus.ACCEPTED, MentoringParticipationStatus.PENDING),false);

        if (participations.isPresent()) {
            MentoringParticipation mp = participations.get();

            if (mp.getParticipationStatus() == MentoringParticipationStatus.ACCEPTED) {
                // ACCEPTED 상태인 경우
                dto.setAuthority(mp.getAuthority());
            } else if (mp.getParticipationStatus() == MentoringParticipationStatus.PENDING) {
                // PENDING 상태인 경우
                dto.setIsParticipate(true);
                dto.setAuthority(MentoringAuthority.NoAuth);
            }
        } else {
            // 결과가 없을 경우
            dto.setIsParticipate(false);
            dto.setAuthority(MentoringAuthority.NoAuth);
        }
    }


}
