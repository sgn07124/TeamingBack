package com.project.Teaming.domain.mentoring.controller;

import com.project.Teaming.domain.mentoring.dto.request.RqBoardDto;
import com.project.Teaming.domain.mentoring.dto.response.MentoringPostStatusDto;
import com.project.Teaming.domain.mentoring.dto.response.RsBoardDto;
import com.project.Teaming.domain.mentoring.dto.response.RsSpecBoardDto;
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

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/mentoring")
public class MentoringBoardController implements SwaggerMentoringBoardController {

    private final MentoringBoardService mentoringBoardService;

    @Override
    @PostMapping("/teams/{team_id}/posts")
    public ResultDetailResponse<String> savePost(@PathVariable Long team_id,
                                               @RequestBody @Valid RqBoardDto dto) {
        return new ResultDetailResponse<>(ResultCode.REGISTER_MENTORING_POST, String.valueOf(mentoringBoardService.saveMentoringPost(team_id, dto)));
    }

    @Override
    @PutMapping("/posts/{post_id}")
    public ResultDetailResponse<RsSpecBoardDto> updatePost(@PathVariable Long post_id,
                                                     @RequestBody @Valid RqBoardDto dto) {
        mentoringBoardService.updateMentoringPost(post_id, dto);
        MentoringBoard mentoringPost = mentoringBoardService.findMentoringPost(post_id);
        return new ResultDetailResponse<>(ResultCode.UPDATE_MENTORING_POST, mentoringBoardService.toDto(mentoringPost));
    }

    @Override
    @GetMapping("/posts")
    public ResultDetailResponse<PaginatedCursorResponse<RsBoardDto>> findAllPosts(@RequestParam(required = false) Long cursor, // 커서
                                                                            @RequestParam(defaultValue = "10") int size ) {
        return new ResultDetailResponse<>(ResultCode.GET_ALL_MENTORING_POSTS, mentoringBoardService.findAllPosts(cursor, size));
    }

    @Override
    @GetMapping("/teams/{team_id}/posts")
    public ResultListResponse<RsBoardDto> findMyAllPosts(@PathVariable Long team_id) {
        return new ResultListResponse<>(ResultCode.GET_ALL_MY_MENTORING_POSTS, mentoringBoardService.findAllMyMentoringPost(team_id));
    }
    @Override
    @PatchMapping("/teams/{team_id}/posts/{post_id}/complete")
    public ResultDetailResponse<MentoringPostStatusDto> completePostStatus(@PathVariable Long team_id, @PathVariable Long post_id) {
        return new ResultDetailResponse<>(ResultCode.UPDATE_POST_STATUS,mentoringBoardService.updatePostStatus(team_id, post_id));
    }
    @Override
    @GetMapping("/posts/{post_id}")
    public ResultDetailResponse<RsSpecBoardDto> findPost(@PathVariable Long post_id) {
        return new ResultDetailResponse<>(ResultCode.GET_MENTORING_POST,
                mentoringBoardService.toDto(mentoringBoardService.findMentoringPost(post_id)));
    }

    @Override
    @DeleteMapping("/posts/{post_id}")
    public ResultDetailResponse<Void> deletePost(@PathVariable Long post_id) {
        mentoringBoardService.deleteMentoringPost(post_id);
        return new ResultDetailResponse<>(ResultCode.DELETE_MENTORING_POST, null);
    }

}
