package com.project.Teaming.domain.mentoring.controller;

import com.project.Teaming.domain.mentoring.dto.request.RqBoardDto;
import com.project.Teaming.domain.mentoring.dto.response.RsBoardDto;
import com.project.Teaming.domain.mentoring.dto.response.RsSpecBoardDto;
import com.project.Teaming.domain.mentoring.entity.*;
import com.project.Teaming.domain.mentoring.service.MentoringBoardService;
import com.project.Teaming.domain.mentoring.service.MentoringParticipationService;
import com.project.Teaming.domain.mentoring.service.MentoringTeamService;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.service.UserService;
import com.project.Teaming.global.error.exception.NoAuthorityException;
import com.project.Teaming.global.jwt.dto.SecurityUserDto;
import com.project.Teaming.global.result.ResultCode;
import com.project.Teaming.global.result.ResultDetailResponse;
import com.project.Teaming.global.result.ResultListResponse;
import com.project.Teaming.global.result.pagenateResponse.PaginatedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/mentoring")
@Tag(name = "MentoringBoard", description = "멘토링 글 관련 API")
public class MentoringBoardController {

    private final MentoringBoardService mentoringBoardService;
    private final MentoringTeamService mentoringTeamService;
    private final MentoringParticipationService mentoringParticipationService;
    private final UserService userService;

    @PostMapping("/{team_id}/post")
    @Operation(summary = "멘토링 글 등록" , description = "멘토링 팀에서(팀의 팀장, 팀원 모두 가능) 글을 등록 할 수 있다. 멘토링 글 id 반환")
    public ResultDetailResponse<Long> savePost(@PathVariable Long team_id,
                                               @RequestBody @Valid RqBoardDto dto) {
        User user = getUser();
        Long savedMentoringPost = mentoringBoardService.saveMentoringPost(user.getId(),team_id, dto);
        return new ResultDetailResponse<>(ResultCode.REGISTER_MENTORING_POST, savedMentoringPost);
    }


    @PostMapping("/{team_id}/post/{post_id}")
    @Operation(summary = "멘토링 글 수정", description = "나의 팀에서 등록된 멘토링 게시물을 팀 구성원(팀장과 팀원) 모두가 수정 할 수 있다. 수정버튼이 있는 멘토링 글 상세페이지로 이동.")
    public ResultDetailResponse<RsSpecBoardDto> updatePost(@PathVariable Long team_id, @PathVariable Long post_id,
                                                     @RequestBody @Valid RqBoardDto dto) {
        User user = getUser();
        MentoringTeam mentoringTeam = mentoringTeamService.findMentoringTeam(team_id);
        MentoringAuthority userAuthority = mentoringBoardService.updateMentoringPost(user.getId(), post_id, dto);
        MentoringBoard mentoringPost = mentoringBoardService.findMentoringPost(post_id);
        RsSpecBoardDto updatePostDto = mentoringPost.toDto(mentoringTeam);
        updatePostDto.setAuthority(userAuthority);
        updatePostDto.setCategory(mentoringTeam.getCategories().stream()
                .map(o -> o.getCategory().getName())
                .collect(Collectors.toList()));
        return new ResultDetailResponse<>(ResultCode.UPDATE_MENTORING_POST, updatePostDto);
    }


    @GetMapping("/posts")
    @Operation(summary = "멘토링 글 모두 조희", description = "모든 멘토링 게시물들을 조희할 수 있다. 멘토링 게시글페이지 보여 줄 때의 API, 한페이지당 4개의 글")
    public ResultDetailResponse<PaginatedResponse<RsBoardDto>> findAllPosts(@RequestParam(defaultValue = "1") int page,
                                                                          @RequestParam(defaultValue = "4") int size,
                                                                          @RequestParam(required = false) MentoringStatus status) {
        PaginatedResponse<RsBoardDto> allPosts = mentoringBoardService.findAllPosts(status, page, size);
        return new ResultDetailResponse<>(ResultCode.GET_ALL_MENTORING_POSTS, allPosts);
    }

    @GetMapping("/{team_Id}/posts")
    @Operation(summary = "특정 멘토링 팀의 모든 글 조회" , description = "특정 멘토링 팀에서 쓴 모든 글을 조회 할 수 있다. 팀 페이지에서 시용")
    public ResultListResponse<RsBoardDto> findMyAllPosts(@PathVariable Long team_Id) {
        List<RsBoardDto> allMyMentoringPost = mentoringBoardService.findAllMyMentoringPost(team_Id);
        return new ResultListResponse<>(ResultCode.GET_ALL_MY_MENTORING_POSTS, allMyMentoringPost);
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
    public ResultDetailResponse<RsSpecBoardDto> findPost(@PathVariable Long post_id) {
        User user = getUser();
        MentoringBoard mentoringPost = mentoringBoardService.findMentoringPost(post_id);
        Optional<MentoringParticipation> teamUser = mentoringParticipationService.findBy(mentoringPost.getMentoringTeam(), user);
        RsSpecBoardDto dto = mentoringPost.toDto(mentoringPost.getMentoringTeam());
        dto.setCategory(mentoringPost.getMentoringTeam().getCategories().stream()
                .map(o -> o.getCategory().getName())
                .collect(Collectors.toList()));
        if (teamUser.isPresent()) {
            dto.setAuthority(teamUser.get().getAuthority());
        } else {
            dto.setAuthority(MentoringAuthority.NoAuth);
        }
        return new ResultDetailResponse<>(ResultCode.GET_MENTORING_POST, dto);
    }

    @DeleteMapping("/post/{post_id}/del")
    @Operation(summary = "멘토링 글 삭제", description = "나의 멘토링 글을 삭제 할 수 있다. 멘토링 게시판으로 이동")
    public ResultDetailResponse<Void> deletePost(@PathVariable Long post_id) {
        User user = getUser();
        mentoringBoardService.deleteMentoringPost(user.getId(),post_id);
        return new ResultDetailResponse<>(ResultCode.DELETE_MENTORING_POST, null);
    }

    private User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUserDto securityUser = (SecurityUserDto) authentication.getPrincipal();
        Long userId = securityUser.getUserId();
        User user = userService.findById(userId).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        return user;
    }

}
