package com.project.Teaming.domain.mentoring.service;

import com.project.Teaming.domain.mentoring.dto.request.RqBoardDto;
import com.project.Teaming.domain.mentoring.entity.MentoringBoard;
import com.project.Teaming.domain.mentoring.entity.MentoringStatus;
import com.project.Teaming.domain.mentoring.entity.MentoringTeam;
import com.project.Teaming.domain.mentoring.entity.Status;
import com.project.Teaming.domain.mentoring.repository.MentoringBoardRepository;
import com.project.Teaming.domain.mentoring.repository.MentoringTeamRepository;
import com.project.Teaming.global.error.exception.MentoringPostNotFoundException;
import com.project.Teaming.global.error.exception.MentoringTeamNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MentoringBoardService {

    private final MentoringBoardRepository mentoringBoardRepository;
    private final MentoringTeamRepository mentoringTeamRepository;

    /**
     * 게시물을 저장하는 로직
     * @param teamId
     * @param boardDto
     */
    @Transactional
    public void saveMentoringPost(Long teamId, RqBoardDto boardDto) {
        MentoringTeam mentoringTeam = mentoringTeamRepository.findById(teamId).orElseThrow(MentoringTeamNotFoundException::new);
        MentoringBoard mentoringBoard = MentoringBoard.builder()
                .title(boardDto.getTitle())
                .contents(boardDto.getContents())
                .startDate(boardDto.getStartDate())
                .endDate(boardDto.getEndDate())
                .role(boardDto.getRole())
                .status(MentoringStatus.RECRUITING)
                .flag(Status.FALSE)
                .build();
        if (!boardDto.getLink().isBlank()) {  //작성한 글에 카카오톡 링크가 있으면 그거 사용, 없으면 멘토링 팀에 등록된 카카오톡 링크 사용
            mentoringBoard.setLink(boardDto.getLink());
        } else mentoringBoard.setLink(mentoringTeam.getLink());
        mentoringBoard.addMentoringBoard(mentoringTeam);  // 멘토링 팀과 연관관계 매핑

        mentoringBoardRepository.save(mentoringBoard);
    }

    /**
     * 특정 게시물을 조회하는 로직
     * 조회수 증가로직 포함
     * @param postId
     * @return
     */
    @Transactional
    public MentoringBoard findMentoringPost(Long postId) {
        MentoringBoard mentoringBoard = mentoringBoardRepository.findById(postId)
                .orElseThrow(() -> new MentoringPostNotFoundException("해당 포스트를 찾을 수 없습니다."));
        if (mentoringBoard.getFlag() == Status.TRUE) {
            throw new MentoringPostNotFoundException("이미 삭제된 포스트 입니다.");
        }
        return mentoringBoard;
    }

    /**
     * 특정 멘토링 팀의 삭제되지않은 모든 게시물들을 가져오는 로직
     * @param teamId
     * @return
     */
    public List<MentoringBoard> findAllMyMentoringPost(Long teamId) {
        MentoringTeam mentoringTeam = mentoringTeamRepository.findById(teamId).orElseThrow(MentoringTeamNotFoundException::new);

        return mentoringTeam.getMentoringBoardList().stream()
                .filter(o -> o.getFlag().equals(Status.FALSE))
                .collect(Collectors.toList());
    }

    /**
     * 삭제되지 않은 모든 게시물들을 가져오는 로직
     * @return
     */
    public List<MentoringBoard> findAllMentoringPost() {
        return mentoringBoardRepository.findAll().stream()
                .filter(o -> o.getFlag().equals(Status.FALSE))
                .collect(Collectors.toList());
    }

    /**
     *  게시물을 수정하는 로직
     * @param postId
     * @param dto
     * @return
     */
    @Transactional
    public void updateMentoringPost(Long postId, RqBoardDto dto) {
        MentoringBoard mentoringBoard = mentoringBoardRepository.findById(postId)
                .orElseThrow(() -> new MentoringPostNotFoundException("해당 포스트를 찾을 수 없습니다."));
        mentoringBoard.updateBoard(dto);
    }


    /**
     * 게시물을 삭제하는 로직
     * @param postId
     */
    @Transactional
    public void deleteMentoringPost(Long postId) {
        MentoringBoard mentoringBoard = mentoringBoardRepository.findById(postId)
                .orElseThrow(() -> new MentoringPostNotFoundException("해당 포스트를 찾을 수 없습니다."));
        mentoringBoard.setFlag(Status.TRUE);
    }

}
