package com.project.Teaming.domain.project.controller;

import com.project.Teaming.domain.project.dto.request.CreatePostDto;
import com.project.Teaming.domain.project.dto.response.ProjectPostInfoDto;
import com.project.Teaming.domain.project.dto.response.ProjectPostListDto;
import com.project.Teaming.domain.project.dto.response.ProjectPostStatusDto;
import com.project.Teaming.domain.project.service.ProjectBoardService;
import com.project.Teaming.global.result.ResultCode;
import com.project.Teaming.global.result.pagenateResponse.PaginatedCursorResponse;
import com.project.Teaming.global.result.ResultListResponse;
import com.project.Teaming.global.result.ResultDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/project")
@Tag(name = "ProjectBoard", description = "프로젝트 팀의 게시글 관련 API")
public class ProjectBoardController {

    private final ProjectBoardService projectBoardService;

    @PostMapping("/post/{team_id}")
    @Operation(summary = "프로젝트 팀 게시물 작성", description = "프로젝트 팀에 대한 모집 게시글 작성")
    public ResultDetailResponse<Void> createPost(@PathVariable Long team_id, @Valid @RequestBody CreatePostDto createPostDto) {
        projectBoardService.createPost(team_id, createPostDto);
        return new ResultDetailResponse<>(ResultCode.REGISTER_PROJECT_POST, null);
    }

    @GetMapping("/post/{team_id}/{post_id}")
    @Operation(summary = "프로젝트 팀 게시물 상세 조회", description = "프로젝트 팀에서 작성한 게시물들 중 하나에 대한 상세 조회")
    public ResultDetailResponse<ProjectPostInfoDto> getPostInfo(@PathVariable Long team_id, @PathVariable Long post_id) {
        ProjectPostInfoDto postInfoDto = projectBoardService.getPostInfo(team_id, post_id);
        return new ResultDetailResponse<>(ResultCode.GET_PROJECT_POST_INFO, postInfoDto);
    }

    @PutMapping("/post/{team_id}/{post_id}/edit")
    @Operation(summary = "프로젝트 팀 게시물 수정", description = "게시물 수정")
    public ResultDetailResponse<Void> updatePost(@PathVariable Long team_id, @PathVariable Long post_id, @Valid @RequestBody CreatePostDto createPostDto) {
        projectBoardService.updatePost(team_id, post_id, createPostDto);
        return new ResultDetailResponse<>(ResultCode.UPDATE_PROJECT_POST_INFO, null);
    }

    @PutMapping("/post/{team_id}/{post_id}/complete")
    @Operation(summary = "게시물 모집 완료", description = "게시물을 팀원 또는 팀장이 모집 완료 처리를 직접 할 수 있다.")
    public ResultDetailResponse<ProjectPostStatusDto> completePostStatus(@PathVariable Long team_id, @PathVariable Long post_id) {
        ProjectPostStatusDto dto = projectBoardService.completePostStatus(team_id, post_id);
        return new ResultDetailResponse<>(ResultCode.GET_PROJECT_POST_STATUS, dto);
    }

    @DeleteMapping("/post/{team_id}/{post_id}")
    @Operation(summary = "프로젝트 팀 게시물 삭제", description = "프로젝트 팀에서 작성한 모집 게시글 삭제.")
    public ResultDetailResponse<Void> deletePost(@PathVariable Long team_id, @PathVariable Long post_id) {
        projectBoardService.deletePost(team_id, post_id);
        return new ResultDetailResponse<>(ResultCode.DELETE_PROJECT_POST_INFO, null);
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
                            name = "pageSize",
                            description = "페이지당 게시글 수(기본값: 10)",
                            required = false,
                            schema = @Schema(type = "integer", example = "10", defaultValue = "10")
                    )
            }
    )
    public ResultDetailResponse<PaginatedCursorResponse<ProjectPostListDto>> getPosts(
            @RequestParam(required = false) Long cursor, // 마지막 게시글 ID
            @RequestParam(defaultValue = "10") int pageSize) {

        PaginatedCursorResponse<ProjectPostListDto> posts = projectBoardService.getProjectPosts(cursor, pageSize);
        return new ResultDetailResponse<>(ResultCode.GET_PROJECT_POST_LIST, posts);
    }

    @GetMapping("/posts/{team_id}")
    @Operation(summary = "프로젝트 팀의 게시글 조회", description = "특정 프로젝트 팀에서 작성한 게시글 목록을 조회")
    public ResultListResponse<ProjectPostListDto> getTeamPosts(@PathVariable Long team_id) {
        List<ProjectPostListDto> posts = projectBoardService.getTeamProjectPosts(team_id);
        return new ResultListResponse<>(ResultCode.GET_PROJECT_POST_LIST, posts);
    }
}
