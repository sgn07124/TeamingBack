package com.project.Teaming.domain.project.controller;

import com.project.Teaming.domain.project.dto.request.CreatePostDto;
import com.project.Teaming.domain.project.service.ProjectBoardService;
import com.project.Teaming.global.result.ResultCode;
import com.project.Teaming.global.result.ResultResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public ResultResponse<Void> createPost(@PathVariable Long team_id, @Valid @RequestBody CreatePostDto createPostDto) {
        projectBoardService.createPost(team_id, createPostDto);
        return new ResultResponse<>(ResultCode.REGISTER_PROJECT_POST, null);
    }
}
