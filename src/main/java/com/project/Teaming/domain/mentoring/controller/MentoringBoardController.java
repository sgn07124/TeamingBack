package com.project.Teaming.domain.mentoring.controller;

import com.project.Teaming.domain.mentoring.dto.request.RqBoardDto;
import com.project.Teaming.domain.mentoring.dto.response.MentoringPostStatusDto;
import com.project.Teaming.domain.mentoring.dto.response.RsBoardDto;
import com.project.Teaming.domain.mentoring.dto.response.RsSpecBoardDto;
import com.project.Teaming.domain.mentoring.entity.*;
import com.project.Teaming.domain.mentoring.service.MentoringBoardService;
import com.project.Teaming.global.result.ResultCode;
import com.project.Teaming.global.result.ResultDetailResponse;
import com.project.Teaming.global.result.ResultListResponse;
import com.project.Teaming.global.result.pagenateResponse.PaginatedCursorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/mentoring")
@Tag(name = "MentoringBoard", description = "멘토링 글 관련 API")
public class MentoringBoardController {

    private final MentoringBoardService mentoringBoardService;

    @PostMapping("/teams/{team_id}/posts")
    @Operation(summary = "멘토링 글 등록" , description = "멘토링 팀에서(팀의 팀장, 팀원 모두 가능) 글을 등록 할 수 있다. 멘토링 글 id 반환")
    public ResultDetailResponse<String> savePost(@PathVariable Long team_id,
                                               @RequestBody @Valid RqBoardDto dto) {
        Long savedMentoringPost = mentoringBoardService.saveMentoringPost(team_id, dto);
        return new ResultDetailResponse<>(ResultCode.REGISTER_MENTORING_POST, String.valueOf(savedMentoringPost));
    }


    @PutMapping("/posts/{post_id}")
    @Operation(summary = "멘토링 글 수정", description = "나의 팀에서 등록된 멘토링 게시물을 팀 구성원(팀장과 팀원) 모두가 수정 할 수 있다. 수정버튼이 있는 멘토링 글 상세페이지로 이동.")
    public ResultDetailResponse<RsSpecBoardDto> updatePost(@PathVariable Long post_id,
                                                     @RequestBody @Valid RqBoardDto dto) {
        mentoringBoardService.updateMentoringPost(post_id, dto);
        MentoringBoard mentoringPost = mentoringBoardService.findMentoringPost(post_id);
        RsSpecBoardDto updatePostDto = mentoringBoardService.toDto(mentoringPost);
        return new ResultDetailResponse<>(ResultCode.UPDATE_MENTORING_POST, updatePostDto);
    }


    @GetMapping("/posts")
    @Operation(
            summary = "게시글 목록 조회",
            description = "커서 기반 페이징을 사용하여 게시글 목록을 조회한다. cursor를 기준으로 이후 게시글을 가져옵니다.",
            parameters = {
                    @Parameter(
                            name = "cursor",
                            description = "다음 게시글의 ID(다음 페이지 조회 시 필요. nextCursor 값)",
                            required = false
                    ),
                    @Parameter(
                            name = "size",
                            description = "페이지당 게시글 수(기본값: 10)",
                            required = false,
                            schema = @Schema(type = "integer", example = "10", defaultValue = "10")
                    )
            }
    )
    public ResultDetailResponse<PaginatedCursorResponse<RsBoardDto>> findAllPosts(@RequestParam(required = false) Long cursor, // 커서
                                                                            @RequestParam(defaultValue = "10") int size ) {
        PaginatedCursorResponse<RsBoardDto> allPosts = mentoringBoardService.findAllPosts(cursor, size);
        return new ResultDetailResponse<>(ResultCode.GET_ALL_MENTORING_POSTS, allPosts);
    }


    @GetMapping("/teams/{team_id}/posts")
    @Operation(summary = "특정 멘토링 팀의 모든 글 조회" , description = "특정 멘토링 팀에서 쓴 모든 글을 조회 할 수 있다. 팀 페이지에서 시용")
    public ResultListResponse<RsBoardDto> findMyAllPosts(@PathVariable Long team_Id) {
        List<RsBoardDto> allMyMentoringPost = mentoringBoardService.findAllMyMentoringPost(team_Id);
        return new ResultListResponse<>(ResultCode.GET_ALL_MY_MENTORING_POSTS, allMyMentoringPost);
    }

    @PatchMapping("/teams/{team_id}/posts/{post_id}/complete")
    @Operation(summary = "게시물 모집 완료 처리", description = "게시물에서 팀구성원이 모집 완료 처리를 직접 할 수 있다.")
    public ResultDetailResponse<MentoringPostStatusDto> completePostStatus(@PathVariable Long team_id, @PathVariable Long post_id) {
        MentoringPostStatusDto statusDto = mentoringBoardService.updatePostStatus(team_id, post_id);
        return new ResultDetailResponse<>(ResultCode.UPDATE_POST_STATUS,statusDto);
    }

    /**
     * 특정 글 조회하는 API
     * 현재 로그인한 유저가 가지고있는 멘토링 팀 ID들을 모두 프론트에게 같이 보내줌
     * 프론트가 ID비교해 내 팀의 글이면 수정하기, 삭제하기 버튼 보여줌.
     * @param post_id
     * @return
     */
    @GetMapping("/posts/{post_id}")
    @Operation(summary = "멘토링 글 조희" , description = "멘토링 게시판에서 특정 멘토링 글을 조회할 수 있다. " +
            "Authority가 LEADER와 CREW이면 수정할 수 있는 페이지, NoAuth이면 수정이 불가능 한 일반사용자용 페이지 보여주세요.")
    public ResultDetailResponse<RsSpecBoardDto> findPost(@PathVariable Long post_id) {
        MentoringBoard mentoringPost = mentoringBoardService.findMentoringPost(post_id);
        RsSpecBoardDto dto = mentoringBoardService.toDto(mentoringPost);
        return new ResultDetailResponse<>(ResultCode.GET_MENTORING_POST, dto);
    }


    @DeleteMapping("/posts/{post_id}")
    @Operation(summary = "멘토링 글 삭제", description = "나의 멘토링 글을 삭제 할 수 있다. 멘토링 게시판으로 이동")
    public ResultDetailResponse<Void> deletePost(@PathVariable Long post_id) {
        mentoringBoardService.deleteMentoringPost(post_id);
        return new ResultDetailResponse<>(ResultCode.DELETE_MENTORING_POST, null);
    }

}
