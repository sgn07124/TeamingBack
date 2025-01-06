package com.project.Teaming.domain.mentoring.controller;

import com.project.Teaming.domain.mentoring.dto.request.BoardRequest;
import com.project.Teaming.domain.mentoring.dto.response.MentoringPostStatusResponse;
import com.project.Teaming.domain.mentoring.dto.response.BoardResponse;
import com.project.Teaming.domain.mentoring.dto.response.BoardSpecResponse;
import com.project.Teaming.domain.mentoring.entity.MentoringBoard;
import com.project.Teaming.domain.mentoring.service.MentoringBoardService;
import com.project.Teaming.global.result.ResultCode;
import com.project.Teaming.global.result.ResultDetailResponse;
import com.project.Teaming.global.result.ResultListResponse;
import com.project.Teaming.global.result.pagenateResponse.PaginatedCursorResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/mentoring")
public class MentoringBoardController implements SwaggerMentoringBoardController {

    private final MentoringBoardService mentoringBoardService;

    @Override
    @PostMapping("/teams/{teamId}/posts")
    public ResultDetailResponse<String> savePost(@PathVariable Long teamId,
                                               @RequestBody @Valid BoardRequest dto) {
        return new ResultDetailResponse<>(ResultCode.REGISTER_MENTORING_POST, String.valueOf(mentoringBoardService.saveMentoringPost(teamId, dto)));
    }

    @Override
    @PutMapping("/posts/{postId}")
    public ResultDetailResponse<BoardSpecResponse> updatePost(@PathVariable Long postId,
                                                              @RequestBody @Valid BoardRequest dto) {
        mentoringBoardService.updateMentoringPost(postId, dto);
        MentoringBoard mentoringPost = mentoringBoardService.findMentoringPost(postId);
        return new ResultDetailResponse<>(ResultCode.UPDATE_MENTORING_POST, mentoringBoardService.toDto(mentoringPost));
    }

    @Override
    @GetMapping("/posts")
    public ResultDetailResponse<PaginatedCursorResponse<BoardResponse>> findAllPosts(@RequestParam(required = false) Long cursor, // 커서
                                                                                     @RequestParam(defaultValue = "10") int size ) {
        return new ResultDetailResponse<>(ResultCode.GET_ALL_MENTORING_POSTS, mentoringBoardService.findAllPosts(cursor, size));
    }

    @Override
    @GetMapping("/teams/{teamId}/posts")
    public ResultListResponse<BoardResponse> findMyAllPosts(@PathVariable Long teamId) {
        return new ResultListResponse<>(ResultCode.GET_ALL_MY_MENTORING_POSTS, mentoringBoardService.findAllMyMentoringPost(teamId));
    }
    @Override
    @PatchMapping("/teams/{teamId}/posts/{postId}/complete")
    public ResultDetailResponse<MentoringPostStatusResponse> completePostStatus(@PathVariable Long teamId, @PathVariable Long postId) {
        return new ResultDetailResponse<>(ResultCode.UPDATE_POST_STATUS,mentoringBoardService.updatePostStatus(teamId, postId));
    }
    @Override
    @GetMapping("/posts/{postId}")
    public ResultDetailResponse<BoardSpecResponse> findPost(@PathVariable Long postId) {
        return new ResultDetailResponse<>(ResultCode.GET_MENTORING_POST,
                mentoringBoardService.toDto(mentoringBoardService.findMentoringPost(postId)));
    }

    @Override
    @DeleteMapping("/posts/{postId}")
    public ResultDetailResponse<Void> deletePost(@PathVariable Long postId) {
        mentoringBoardService.deleteMentoringPost(postId);
        return new ResultDetailResponse<>(ResultCode.DELETE_MENTORING_POST, null);
    }

}
