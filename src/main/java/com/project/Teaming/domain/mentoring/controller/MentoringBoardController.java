package com.project.Teaming.domain.mentoring.controller;

import com.project.Teaming.domain.mentoring.dto.request.RqBoardDto;
import com.project.Teaming.domain.mentoring.dto.response.RsBoardDto;
import com.project.Teaming.domain.mentoring.dto.response.RsSpecBoardDto;
import com.project.Teaming.domain.mentoring.entity.*;
import com.project.Teaming.domain.mentoring.service.MentoringBoardService;
import com.project.Teaming.domain.mentoring.service.MentoringTeamService;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.service.UserService;
import com.project.Teaming.global.error.exception.NoAuthorityException;
import com.project.Teaming.global.jwt.dto.SecurityUserDto;
import com.project.Teaming.global.result.ResultCode;
import com.project.Teaming.global.result.ResultResponse;
import com.project.Teaming.global.result.pagenateResponse.PaginatedResponse;
import com.project.Teaming.global.result.pagenateResponse.ResultPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/mentoring")
@Tag(name = "MentoringBoard", description = "멘토링 글 관련 API")
public class MentoringBoardController {

    private final MentoringBoardService mentoringBoardService;
    private final MentoringTeamService mentoringTeamService;
    private final UserService userService;

    @PostMapping("/{team_id}/post")
    @Operation(summary = "멘토링 글 등록" , description = "멘토링 팀에서(팀의 팀장, 팀원 모두 가능) 글을 등록 할 수 있다. 멘토링 게시판 페이지로 이동")
    public ResultResponse<Long> savePost(@PathVariable Long team_id,
                                         @RequestBody @Valid RqBoardDto dto) {
        User user = getUser();
        MentoringTeam mentoringTeam = mentoringTeamService.findMentoringTeam(team_id);
        List<MentoringParticipation> team = getTeam(mentoringTeam, user);
        if (!ObjectUtils.isEmpty(team)) {
            Long savedMentoringPost = mentoringBoardService.saveMentoringPost(team_id, dto);
            return new ResultResponse<>(ResultCode.REGISTER_MENTORING_POST, List.of(savedMentoringPost));
        } else throw new NoAuthorityException("글을 등록 할 수 있는 권한이 없습니다.");

    }


    @PostMapping("/{team_id}/post/{post_id}")
    @Operation(summary = "멘토링 글 수정", description = "나의 팀에서 등록된 멘토링 게시물을 팀 구성원(팀장과 팀원) 모두가 수정 할 수 있다. 수정버튼이 있는 멘토링 글 상세페이지로 이동.")
    public ResultResponse<RsSpecBoardDto> updatePost(@PathVariable Long team_id, @PathVariable Long post_id,
                                                     @RequestBody @Valid RqBoardDto dto) {
        User user = getUser();
        MentoringTeam mentoringTeam = mentoringTeamService.findMentoringTeam(team_id);
        List<MentoringParticipation> team = getTeam(mentoringTeam, user);
        if (!ObjectUtils.isEmpty(team)) {
            mentoringBoardService.updateMentoringPost(post_id,dto);
            MentoringBoard mentoringPost = mentoringBoardService.findMentoringPost(post_id);
            RsSpecBoardDto updatePostDto = mentoringPost.toDto();
            updatePostDto.setAuthority(team.get(0).getAuthority());
            updatePostDto.setCategory(mentoringTeam.getCategories().stream()
                    .map(o -> o.getCategory().getName())
                    .collect(Collectors.toList()));
            return new ResultResponse<>(ResultCode.UPDATE_MENTORING_POST, List.of(updatePostDto));
        } else throw new NoAuthorityException("글을 수정 할 수 있는 권한이 없습니다.");
    }


    @GetMapping("/posts")
    @Operation(summary = "멘토링 글 모두 조희", description = "모든 멘토링 게시물들을 조희할 수 있다. 멘토링 게시글페이지 보여 줄 때의 API, 한페이지당 4개의 글")
    public ResultPageResponse<PaginatedResponse<RsBoardDto>> findAllPosts(@RequestParam(defaultValue = "1") int page,
                                                                                   @RequestParam(defaultValue = "4") int size,
                                                                                   @RequestParam(required = false) MentoringStatus status) {
        PaginatedResponse<RsBoardDto> allPosts = mentoringBoardService.findAllPosts(status, page, size);

        return new ResultPageResponse<>(ResultCode.GET_ALL_MENTORING_POSTS, allPosts);
    }

    @GetMapping("/{team_Id}/posts")
    @Operation(summary = "특정 멘토링 팀의 모든 글 조회" , description = "특정 멘토링 팀에서 쓴 모든 글을 조회 할 수 있다. 팀 페이지에서 시용")
    public ResultResponse<RsBoardDto> findMyAllPosts(@PathVariable Long team_Id) {
        List<RsBoardDto> myBoards = mentoringBoardService.findAllMyMentoringPost(team_Id)
                .stream().map(
                        o -> RsBoardDto.builder()
                                .id(o.getId())
                                .title(o.getTitle())
                                .startDate(o.getMentoringTeam().getStartDate())
                                .endDate(o.getMentoringTeam().getEndDate())
                                .contents(o.getContents())
                                .category(o.getMentoringTeam().getCategories().stream()
                                        .map(x -> x.getCategory().getName())
                                        .collect(Collectors.toList()))
                                .build())
                .collect(Collectors.toList());
        return new ResultResponse<>(ResultCode.GET_ALL_MY_MENTORING_POSTS, myBoards);
    }

    /**
     * 특정 글 조회하는 API
     * 현재 로그인한 유저가 가지고있는 멘토링 팀 ID들을 모두 프론트에게 같이 보내줌
     * 프론트가 ID비교해 내 팀의 글이면 수정하기, 삭제하기 버튼 보여줌.
     * @param post_id
     * @return
     */
    @GetMapping("/post/{post_id}")
    @Operation(summary = "멘토링 글 조희" , description = "멘토링 게시판에서 특정 멘토링 글을 조회할 수 있다. " +
            "Authority가 LEADER와 CREW이면 수정할 수 있는 페이지, NoAuth이면 수정이 불가능 한 일반사용자용 페이지 보여주세요.")
    public ResultResponse<RsSpecBoardDto> findPost(@PathVariable Long post_id) {
        User user = getUser();
        MentoringBoard mentoringPost = mentoringBoardService.findMentoringPost(post_id);
        MentoringTeam mentoringTeam = mentoringPost.getMentoringTeam();
        List<MentoringParticipation> team = getTeam(mentoringTeam, user);
        RsSpecBoardDto dto = mentoringPost.toDto();
        dto.setCategory(mentoringTeam.getCategories().stream()
                .map(o -> o.getCategory().getName())
                .collect(Collectors.toList()));
        if (!ObjectUtils.isEmpty(team)) {
            dto.setAuthority(team.get(0).getAuthority());
        } else {
            dto.setAuthority(MentoringAuthority.NoAuth);
        }
        return new ResultResponse<>(ResultCode.GET_MENTORING_POST, List.of(dto));
    }

    @DeleteMapping("/{team_id}/post/{post_id}/del")
    @Operation(summary = "멘토링 글 삭제", description = "나의 멘토링 글을 삭제 할 수 있다. 멘토링 게시판으로 이동")
    public ResultResponse<Void> deletePost(@PathVariable Long team_id, @PathVariable Long post_id) {
        User user = getUser();
        MentoringTeam mentoringTeam = mentoringTeamService.findMentoringTeam(team_id);
        List<MentoringParticipation> team = getTeam(mentoringTeam, user);
        if (!ObjectUtils.isEmpty(team)) {
            mentoringBoardService.deleteMentoringPost(post_id);
        } else throw new NoAuthorityException("글을 삭제할 수 있는 권한이 없습니다.");
        return new ResultResponse<>(ResultCode.DELETE_MENTORING_POST, null);
    }


    /**
     * 내가 해당 팀 구성원인지 확인해주는 로직
     *
     * @param user
     * @return
     */
    private List<MentoringParticipation> getTeam(MentoringTeam mentoringTeam, User user) {
        return user.getMentoringParticipations().stream()
                .filter(o -> o.getMentoringTeam().equals(mentoringTeam))
                .toList();
    }

    private User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUserDto securityUser = (SecurityUserDto) authentication.getPrincipal();
        Long userId = securityUser.getUserId();
        User user = userService.findById(userId).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        return user;
    }

}
