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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MentoringBoardService {

    private final MentoringBoardRepository mentoringBoardRepository;
    private final MentoringTeamRepository mentoringTeamRepository;

    /**
     * 게시물을 저장하는 로직
     * 저장된 post Id 반환
     * @param teamId
     * @param boardDto
     */
    @Transactional
    public Long saveMentoringPost(Long teamId, RqBoardDto boardDto) {
        MentoringTeam mentoringTeam = mentoringTeamRepository.findById(teamId).orElseThrow(MentoringTeamNotFoundException::new);  //명시적 조회, 최신 데이터 반영
        MentoringBoard mentoringBoard = MentoringBoard.builder()
                .title(boardDto.getTitle())
                .contents(boardDto.getContents())
                .role(boardDto.getRole())
                .mentoringCnt(boardDto.getMentoringCnt())
                .build();
        mentoringBoard.setLink(Optional.ofNullable(boardDto.getLink())
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
    @Transactional
    public MentoringBoard findMentoringPost(Long postId) {
        MentoringBoard mentoringBoard = mentoringBoardRepository.findById(postId)
                .orElseThrow(() -> new MentoringPostNotFoundException("이미 삭제되었거나 존재하지 않는 글 입니다."));
        if (mentoringBoard.getMentoringTeam().getFlag() == Status.FALSE) { //team의 최신 데이터 업데이트
            return mentoringBoard;
        } else {
            throw new MentoringTeamNotFoundException("이미 삭제된 팀의 글 입니다.");
        }
    }

    /**
     * 특정 멘토링 팀의 삭제되지않은 모든 게시물들을 가져오는 로직
     * @param teamId
     * @return
     */
    public List<MentoringBoard> findAllMyMentoringPost(Long teamId) {
        MentoringTeam mentoringTeam = mentoringTeamRepository.findById(teamId).orElseThrow(MentoringTeamNotFoundException::new); //명시적 조회, 최신 데이터 반영
        if (mentoringTeam.getFlag() == Status.FALSE) {
            return mentoringTeam.getMentoringBoardList();
        }
        else throw new MentoringTeamNotFoundException("이미 삭제된 팀 입니다.");
    }

    /**
     * 삭제되지 않은 모든 게시물들을 가져오는 로직
     * @return
     */
    public Page<MentoringBoard> findAllMentoringPost(Pageable pageable) {
        return mentoringBoardRepository.findAll(pageable);
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
                .orElseThrow(() -> new MentoringPostNotFoundException("이미 삭제되었거나 존재하지 않는 글 입니다."));
        if (mentoringBoard.getMentoringTeam().getFlag() == Status.FALSE) {  //team의 최신 데이터 업데이트
            mentoringBoard.updateBoard(dto);
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
        MentoringBoard mentoringBoard = mentoringBoardRepository.findById(postId)
                .orElseThrow(() -> new MentoringPostNotFoundException("이미 삭제되었거나 존재하지 않는 글 입니다."));
        if (mentoringBoard.getMentoringTeam().getFlag() == Status.FALSE) { //team의 최신 데이터 업데이트
            mentoringBoardRepository.delete(mentoringBoard);
        } else {
            throw new MentoringTeamNotFoundException("이미 삭제된 팀의 글 입니다.");
        }
    }

}
