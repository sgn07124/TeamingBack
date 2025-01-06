package com.project.Teaming.domain.mentoring.controller;

import com.project.Teaming.domain.mentoring.dto.request.BoardRequest;
import com.project.Teaming.domain.mentoring.dto.response.MentoringPostStatusResponse;
import com.project.Teaming.domain.mentoring.dto.response.BoardResponse;
import com.project.Teaming.domain.mentoring.dto.response.BoardSpecResponse;
import com.project.Teaming.domain.mentoring.entity.MentoringAuthority;
import com.project.Teaming.domain.mentoring.entity.MentoringRole;
import com.project.Teaming.domain.mentoring.entity.PostStatus;
import com.project.Teaming.global.result.ResultCode;
import com.project.Teaming.global.result.ResultDetailResponse;
import com.project.Teaming.global.result.ResultListResponse;
import com.project.Teaming.global.result.pagenateResponse.PaginatedCursorResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/mock/mentoring")
public class MentoringBoardMockController {

    @PostMapping("/{team_id}/post")
    @Operation(summary = "멘토링 글 등록(Mock)", description = "팀 ID 기반으로 임의의 글 ID를 반환하는 목 API입니다.")
    public ResultDetailResponse<String> savePostMock(
            @PathVariable Long team_id,
            @RequestBody @Valid BoardRequest dto) {

        // 50 이하의 랜덤 값 생성
        Random random = new Random();
        int mockPostId = random.nextInt(50) + 1; // 1부터 50까지 랜덤 값 생성

        // 랜덤 ID를 문자열로 변환하여 반환
        return new ResultDetailResponse<>(ResultCode.REGISTER_MENTORING_POST, String.valueOf(mockPostId));
    }

    @PostMapping("/post/{post_id}")
    @Operation(summary = "멘토링 글 수정(Mock)", description = "Mock 데이터로 멘토링 게시물을 수정한 결과를 반환합니다.")
    public ResultDetailResponse<BoardSpecResponse> updatePostMock(
            @PathVariable Long post_id,
            @RequestBody @Valid BoardRequest dto) {

        // Mock 데이터 생성
        BoardSpecResponse mockResponse = new BoardSpecResponse(
                MentoringAuthority.LEADER, // 고정된 권한
                null, // `isParticipate`는 고정된 권한이 LEADER이므로 null
                post_id, // 요청받은 post_id를 그대로 사용
                7L, // 고정된 팀 ID
                dto.getTitle(), // 수정된 제목
                "Mock 팀 이름", // 목데이터에서 고정된 팀 이름
                dto.getDeadLine(), // 요청에서 전달된 모집 마감기한
                PostStatus.RECRUITING, // 고정된 상태
                LocalDate.of(2024, 1, 1), // 목데이터로 고정된 시작일
                LocalDate.of(2024, 6, 30), // 목데이터로 고정된 종료일
                dto.getRole(), // 요청에서 전달된 모집 역할
                dto.getMentoringCnt(), // 요청에서 전달된 멘토링 횟수
                dto.getLink(), // 요청에서 전달된 연락 방법
                List.of("1", "2", "3"), // 목데이터로 고정된 카테고리
                dto.getContents(), // 요청에서 전달된 모집글 내용
                LocalDateTime.of(2024, 12, 22, 10, 0), // 고정된 생성일
                LocalDateTime.now() // 현재 시간으로 수정일 설정
        );

        // Mock 데이터 반환
        return new ResultDetailResponse<>(ResultCode.UPDATE_MENTORING_POST, mockResponse);
    }

    @GetMapping("/posts")
    @Operation(
            summary = "게시글 목록 조회(Mock)",
            description = "Mock 데이터로 게시글 목록을 조회합니다. 페이징 정보를 포함합니다."
    )
    public ResultDetailResponse<PaginatedCursorResponse<BoardResponse>> findAllPostsMock(
            @RequestParam(required = false) Long cursor, // 커서
            @RequestParam(defaultValue = "10") int size) {

        // Mock 게시글 리스트 생성
        List<BoardResponse> mockPosts = new ArrayList<>();
        Random random = new Random(); // 랜덤 객체 생성

        // 시작점과 끝점 계산
        long start = (cursor != null) ? cursor : 50; // 기본적으로 첫 요청은 50부터 시작
        long end = Math.max(start - size + 1, 1); // 최소값을 1로 설정

        for (long i = start; i >= end; i--) { // 역순으로 게시글 생성
            mockPosts.add(
                    new BoardResponse(
                            i, // ID 생성
                            "Mock 제목 " + i,
                            "Mock 팀 이름 " + i,
                            LocalDate.of(2024, 1, 1), // 고정된 시작일
                            LocalDate.of(2024, 6, 30), // 고정된 종료일
                            generateTeamRandomCategories(random), // 랜덤한 카테고리 리스트
                            "Mock 게시글 내용 " + i,
                            random.nextBoolean() ? PostStatus.RECRUITING : PostStatus.COMPLETE // 랜덤 상태
                    )
            );
        }

        // Mock 페이징 정보 생성
        Long nextCursor = (end > 1) ? end - 1 : null; // 다음 커서 값, 마지막 페이지면 null
        boolean isLast = (end <= 1); // 마지막 페이지 여부

        PaginatedCursorResponse<BoardResponse> mockResponse = new PaginatedCursorResponse<>(
                mockPosts, // 게시글 리스트
                nextCursor, // 다음 커서 값
                size, // 페이지 크기
                isLast // 마지막 페이지 여부
        );

        // Mock 데이터 반환
        return new ResultDetailResponse<>(ResultCode.GET_ALL_MENTORING_POSTS, mockResponse);
    }



    @GetMapping("/{team_Id}/posts")
    @Operation(summary = "특정 멘토링 팀의 모든 글 조회(Mock)", description = "Mock 데이터로 특정 멘토링 팀의 게시글을 조회합니다.")
    public ResultListResponse<BoardResponse> findMyAllPostsMock(@PathVariable Long team_Id) {

        // Mock 게시글 리스트 생성
        List<BoardResponse> mockPosts = new ArrayList<>();
        Random random = new Random(); // 랜덤 객체 생성

        for (int i = 1; i <= 5; i++) { // 5개의 게시글 생성
            mockPosts.add(
                    new BoardResponse(
                            (team_Id * 100) + i, // 팀 ID를 기반으로 고유한 ID 생성
                            "Mock 팀 게시글 제목 " + i,
                            "Mock 팀 이름 " + team_Id,
                            LocalDate.of(2024, 1, i), // 고정된 시작일
                            LocalDate.of(2024, 6, i), // 고정된 종료일
                            generateTeamRandomCategories(random), // 랜덤 카테고리 생성
                            "Mock 게시글 내용 " + i,
                            i % 2 == 0 ? PostStatus.RECRUITING : PostStatus.COMPLETE // 짝수는 RECRUITING, 홀수는 COMPLETE
                    )
            );
        }

        // Mock 데이터 반환
        return new ResultListResponse<>(ResultCode.GET_ALL_MY_MENTORING_POSTS, mockPosts);
    }

    // 랜덤한 카테고리 리스트 생성 메서드
    private List<String> generateTeamRandomCategories(Random random) {
        int numCategories = random.nextInt(2) + 1; // 1~2개의 카테고리 선택
        Set<Integer> categoriesSet = new HashSet<>(); // 중복 방지를 위한 Set 사용

        while (categoriesSet.size() < numCategories) {
            categoriesSet.add(random.nextInt(11) + 1); // 1~11 범위의 숫자 추가
        }

        // Set을 List로 변환 후 문자열로 변환
        return categoriesSet.stream()
                .map(String::valueOf)
                .collect(Collectors.toList());
    }

    @PostMapping("/post/{team_id}/{post_id}/complete")
    @Operation(summary = "게시물 모집 완료 처리(Mock)", description = "Mock 데이터로 게시글 상태를 COMPLETE로 변경한 결과를 반환합니다.")
    public ResultDetailResponse<MentoringPostStatusResponse> completePostStatusMock(
            @PathVariable Long team_id,
            @PathVariable Long post_id) {

        // Mock 데이터 생성
        MentoringPostStatusResponse mockStatusDto = new MentoringPostStatusResponse(PostStatus.COMPLETE);

        // Mock 데이터 반환
        return new ResultDetailResponse<>(ResultCode.UPDATE_POST_STATUS, mockStatusDto);
    }

    @GetMapping("/post/{post_id}")
    @Operation(summary = "멘토링 글 조회(Mock)", description = "Mock 데이터로 특정 멘토링 글의 정보를 반환합니다.")
    public ResultDetailResponse<BoardSpecResponse> findPostMock(@PathVariable Long post_id) {
        // 랜덤 객체 생성
        Random random = new Random();

        // 랜덤 권한 설정
        MentoringAuthority randomAuthority = generateRandomAuthority(random);

        // Mock 데이터 생성
        BoardSpecResponse mockPost = new BoardSpecResponse(
                randomAuthority, // 랜덤 권한 설정
                randomAuthority == MentoringAuthority.NoAuth ? (random.nextBoolean() ? true : null) : null, // NoAuth일 때만 랜덤
                post_id, // 요청받은 post_id를 그대로 사용
                7L, // 고정된 팀 ID
                "Mock 게시글 제목", // 고정된 제목
                "Mock 팀 이름", // 고정된 팀 이름
                LocalDate.of(2024, 12, 31), // 고정된 마감일
                PostStatus.RECRUITING, // 고정된 상태
                LocalDate.of(2024, 1, 1), // 고정된 시작일
                LocalDate.of(2024, 6, 30), // 고정된 종료일
                MentoringRole.MENTEE, // 고정된 역할
                5, // 고정된 멘토링 횟수
                "http://example.com", // 고정된 링크
                List.of("1", "2"), // 고정된 카테고리
                "Mock 게시글 내용", // 고정된 내용
                LocalDateTime.of(2024, 12, 22, 10, 0), // 고정된 생성일
                LocalDateTime.now() // 현재 시간으로 수정일 설정
        );
        // Mock 데이터 반환
        return new ResultDetailResponse<>(ResultCode.GET_MENTORING_POST, mockPost);
    }

    // 랜덤 Authority 생성 메서드
    private MentoringAuthority generateRandomAuthority(Random random) {
        MentoringAuthority[] authorities = MentoringAuthority.values(); // Enum 값 배열 가져오기
        return authorities[random.nextInt(authorities.length)]; // 랜덤 인덱스로 Authority 선택
    }


    @DeleteMapping("/post/{post_id}/del")
    @Operation(summary = "멘토링 글 삭제(Mock)", description = "Mock 데이터로 특정 멘토링 글 삭제 결과를 반환합니다.")
    public ResultDetailResponse<Void> deletePostMock(@PathVariable Long post_id) {

        // Mock 데이터 반환 (삭제 결과만 포함)
        return new ResultDetailResponse<>(ResultCode.DELETE_MENTORING_POST, null);
    }

}
