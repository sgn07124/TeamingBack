package com.project.Teaming.domain.mentoring.service;

import com.project.Teaming.domain.mentoring.dto.request.RqBoardDto;
import com.project.Teaming.domain.mentoring.dto.response.MentoringPostStatusDto;
import com.project.Teaming.domain.mentoring.dto.response.RsBoardDto;
import com.project.Teaming.domain.mentoring.dto.response.RsSpecBoardDto;
import com.project.Teaming.domain.mentoring.entity.*;
import com.project.Teaming.domain.mentoring.repository.MentoringBoardRepository;
import com.project.Teaming.domain.mentoring.repository.MentoringParticipationRepository;
import com.project.Teaming.domain.mentoring.repository.MentoringTeamRepository;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.repository.UserRepository;
import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.exception.BusinessException;
import com.project.Teaming.global.error.exception.MentoringPostNotFoundException;
import com.project.Teaming.global.error.exception.MentoringTeamNotFoundException;
import com.project.Teaming.global.error.exception.NoAuthorityException;
import com.project.Teaming.global.jwt.dto.SecurityUserDto;
import com.project.Teaming.global.result.pagenateResponse.PaginatedResponse;
import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
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
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MentoringBoardService {

    private final MentoringBoardRepository mentoringBoardRepository;
    private final MentoringTeamRepository mentoringTeamRepository;
    private final UserRepository userRepository;
    private final MentoringParticipationRepository mentoringParticipationRepository;

    /**
     * 게시물을 저장하는 로직
     * 저장된 post Id 반환
     * @param teamId
     * @param boardDto
     */
    @Transactional
    public Long saveMentoringPost(Long teamId, RqBoardDto boardDto) {
        User user = getUser();
        MentoringTeam mentoringTeam = mentoringTeamRepository.findById(teamId).orElseThrow(MentoringTeamNotFoundException::new);  //명시적 조회, 최신 데이터 반영
        Optional<MentoringParticipation> TeamUser = mentoringParticipationRepository.findByMentoringTeamAndUserAndParticipationStatus(mentoringTeam, user, MentoringParticipationStatus.ACCEPTED);
        if (TeamUser.isPresent() && !TeamUser.get().getIsDeleted()) {
            MentoringBoard mentoringBoard = MentoringBoard.builder()
                    .title(boardDto.getTitle())
                    .contents(boardDto.getContents())
                    .role(boardDto.getRole())
                    .status(PostStatus.RECRUITING)
                    .deadLine(boardDto.getDeadLine())
                    .mentoringCnt(boardDto.getMentoringCnt())
                    .build();
            mentoringBoard.setLink(Optional.ofNullable(boardDto.getLink())
                    .orElse(mentoringTeam.getLink()));
            mentoringBoard.addMentoringBoard(mentoringTeam);  // 멘토링 팀과 연관관계 매핑

            MentoringBoard savedPost = mentoringBoardRepository.save(mentoringBoard);
            return savedPost.getId();
        } else throw new NoAuthorityException(ErrorCode.NO_AUTHORITY);
    }

    /**
     * 특정 게시물을 조회하는 로직
     * @param postId
     * @return
     */

    public MentoringBoard findMentoringPost(Long postId) {
        MentoringBoard mentoringBoard = mentoringBoardRepository.findById(postId)
                .orElseThrow(() -> new MentoringPostNotFoundException("이미 삭제되었거나 존재하지 않는 글 입니다."));
        if (mentoringBoard.getMentoringTeam().getFlag() == Status.FALSE) { //team의 최신 데이터 업데이트
            return mentoringBoard;
        } else {
            throw new MentoringTeamNotFoundException("이미 삭제된 팀의 글 입니다.");
        }
    }

    public List<String> findTeamCategories(Long teamId) {
        List<Object[]> teamCategories = mentoringBoardRepository.findAllCategoriesByMentoringTeamId(teamId);
        return teamCategories.stream()
                .map(x -> String.valueOf(x[1]))
                .distinct()
                .collect(Collectors.toList());
    }


    public RsSpecBoardDto toDto(MentoringBoard mentoringPost) {
        User user = getUser();
        MentoringTeam mentoringTeam = mentoringPost.getMentoringTeam();
        RsSpecBoardDto dto = mentoringPost.toDto(mentoringTeam);
        List<String> teamCategories = findTeamCategories(mentoringTeam.getId());
        dto.setCategory(teamCategories);
        Optional<MentoringParticipation> teamUser = mentoringParticipationRepository.findByMentoringTeamAndUserAndParticipationStatus(mentoringPost.getMentoringTeam(), user,MentoringParticipationStatus.ACCEPTED);
        if (teamUser.isPresent() && !teamUser.get().getIsDeleted()) {
            dto.setAuthority(teamUser.get().getAuthority());
        } else {
            dto.setAuthority(MentoringAuthority.NoAuth);
        }
        return dto;
    }

    /**
     * 특정 멘토링 팀의 삭제되지않은 모든 게시물들을 가져오는 로직
     * @param teamId
     * @return
     */
    public List<RsBoardDto> findAllMyMentoringPost(Long teamId) {
        List<RsBoardDto> boards = mentoringBoardRepository.findAllByMentoringTeamId(teamId);
        List<Object[]> categoryResults = mentoringBoardRepository.findAllCategoriesByMentoringTeamId(teamId);
        MentoringTeam mentoringTeam = mentoringTeamRepository.findById(teamId).orElseThrow(MentoringTeamNotFoundException::new);
        if (mentoringTeam.getFlag() == Status.FALSE) {
            Map<Long, List<String>> categoryMap = new HashMap<>();
            for (Object[] row : categoryResults) {
                Long boardId = (Long) row[0];
                String categoryId = row[1] != null ? String.valueOf(row[1]) : null;
                categoryMap.computeIfAbsent(boardId, k -> new ArrayList<>()).add(categoryId);
            }
            boards.forEach(post -> {
                List<String> categories = categoryMap.getOrDefault(post.getId(), Collections.emptyList());
                post.setCategory(categories);
            });
            return boards;
        }else throw new MentoringTeamNotFoundException("이미 삭제 된 멘토링 팀 입니다.");
    }

    /**
     * 삭제되지 않은 모든 게시물들을 가져오는 로직
     *
     * @return
*/
    public PaginatedResponse<RsBoardDto> findAllPosts(MentoringStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdDate"));

        List<Long> boardIds = mentoringBoardRepository.findMentoringBoardIds(status, Status.FALSE, pageable);  //삭제되지 않은 글들의 id

        List<Tuple> results = mentoringBoardRepository.findAllByIds(boardIds);
        //메모리에서 그룹화,정렬 해줌, db에서 하면 집계되어 중복데이터
        Map<Long, List<Tuple>> groupedResults = results.stream()
                .collect(Collectors.groupingBy(tuple -> tuple.get(QMentoringBoard.mentoringBoard.id)));

        List<RsBoardDto> dtoResults = boardIds.stream()
                .map(boardId -> {
                    List<Tuple> groupedTuples = groupedResults.get(boardId);
                    if (groupedTuples == null || groupedTuples.isEmpty()) {
                        return null;
                    }
                    Tuple firstTuple = groupedTuples.get(0);  //카테고리가 여러개여서 카테고리수만큼 중복 데이터가 생김. 하나만 활용

                    // 각 보드에 대해 카테고리 리스트 생성
                    List<String> categories = groupedTuples.stream()
                            .map(tuple -> String.valueOf(tuple.get(QCategory.category.id)))
                            .filter(Objects::nonNull)
                            .distinct()
                            .collect(Collectors.toList());

                    // RsBoardDto 객체 생성
                    return new RsBoardDto(
                            firstTuple.get(QMentoringBoard.mentoringBoard.id),
                            firstTuple.get(QMentoringBoard.mentoringBoard.title),
                            firstTuple.get(QMentoringTeam.mentoringTeam.name),
                            firstTuple.get(QMentoringTeam.mentoringTeam.startDate),
                            firstTuple.get(QMentoringTeam.mentoringTeam.endDate),
                            categories,
                            firstTuple.get(QMentoringBoard.mentoringBoard.contents)
                    );
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());


        long total = mentoringBoardRepository.countAllByStatus(status, Status.FALSE);

        Page<RsBoardDto> pageDto = new PageImpl<>(dtoResults, pageable, total);
        return new PaginatedResponse<>(
                pageDto.getContent(),
                pageDto.getTotalPages(),
                pageDto.getTotalElements(),
                pageDto.getSize(),
                pageDto.getNumber(),
                pageDto.isFirst(),
                pageDto.isLast(),
                pageDto.getNumberOfElements()
        );

    }

    /**
     *  게시물을 수정하는 로직
     * @param postId
     * @param dto
     * @return
     */
    @Transactional
    public void updateMentoringPost(Long postId, RqBoardDto dto) {
        User user = getUser();
        MentoringBoard mentoringBoard = mentoringBoardRepository.findById(postId)
                .orElseThrow(() -> new MentoringPostNotFoundException("이미 삭제되었거나 존재하지 않는 글 입니다."));
        MentoringTeam mentoringTeam = mentoringBoard.getMentoringTeam();
        Optional<MentoringParticipation> TeamUser = mentoringParticipationRepository.findByMentoringTeamAndUserAndParticipationStatus(mentoringTeam, user, MentoringParticipationStatus.ACCEPTED);
        if (mentoringTeam.getFlag() == Status.FALSE) {  //team의 최신 데이터 업데이트
            if (TeamUser.isPresent() && !TeamUser.get().getIsDeleted()) {
                mentoringBoard.updateBoard(dto);
            } else throw new BusinessException(ErrorCode.NO_AUTHORITY);
        } else {
                throw new MentoringTeamNotFoundException("이미 삭제된 팀의 글 입니다.");
            }
    }

    /**
     * 게시물을 삭제하는 로직
     * @param postId
     */
    @Transactional
    public void deleteMentoringPost(Long postId) {
        User user = getUser();
        MentoringBoard mentoringBoard = mentoringBoardRepository.findById(postId)
                .orElseThrow(() -> new MentoringPostNotFoundException("이미 삭제되었거나 존재하지 않는 글 입니다."));
        Optional<MentoringParticipation> teamUser = mentoringParticipationRepository.findByMentoringTeamAndUserAndParticipationStatus(mentoringBoard.getMentoringTeam(), user, MentoringParticipationStatus.ACCEPTED);
        if (teamUser.isPresent() && !teamUser.get().getIsDeleted()) {
            if (mentoringBoard.getMentoringTeam().getFlag() == Status.FALSE) { //team의 최신 데이터 업데이트
                mentoringBoardRepository.delete(mentoringBoard);
            } else {
                throw new MentoringTeamNotFoundException("이미 삭제된 팀의 글 입니다.");
            }
        }
        else throw new NoAuthorityException(ErrorCode.NO_AUTHORITY);
    }

    /**
     * 리더가 글에서 모집 현황을 수정할 수 있는 로직
     * @param teamId
     * @param postId
     * @return
     */
    @Transactional
    public MentoringPostStatusDto updatePostStatus(Long teamId, Long postId) {
        User user = getUser();
        MentoringTeam mentoringTeam = mentoringTeamRepository.findById(teamId).orElseThrow(MentoringTeamNotFoundException::new);
        Optional<MentoringParticipation> teamLeader = mentoringParticipationRepository.findByMentoringTeamAndUserAndAuthority(mentoringTeam, user, MentoringAuthority.LEADER);
        MentoringBoard post = mentoringBoardRepository.findById(postId).orElseThrow(MentoringPostNotFoundException::new);
        if (teamLeader.isPresent()) {
            post.updateStatus();
            return new MentoringPostStatusDto(PostStatus.COMPLETE);
        } else {
            throw new NoAuthorityException(ErrorCode.NOT_A_MEMBER);
        }
    }

    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정 실행
    @Transactional
    public void updateCheckCompleteStatus() {
        mentoringBoardRepository.bulkUpDateStatus(PostStatus.COMPLETE, PostStatus.RECRUITING, LocalDate.now());
    }

    private User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUserDto securityUser = (SecurityUserDto) authentication.getPrincipal();
        Long userId = securityUser.getUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        return user;
    }

}
