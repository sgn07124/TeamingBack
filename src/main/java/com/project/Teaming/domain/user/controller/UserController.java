package com.project.Teaming.domain.user.controller;

import com.project.Teaming.domain.mentoring.dto.response.MyTeamDto;
import com.project.Teaming.domain.mentoring.entity.MentoringTeam;
import com.project.Teaming.domain.mentoring.service.MentoringTeamService;
import com.project.Teaming.domain.user.dto.request.RegisterDto;
import com.project.Teaming.domain.user.dto.request.UpdateUserInfoDto;
import com.project.Teaming.domain.user.dto.response.ReviewDto;
import com.project.Teaming.domain.user.dto.response.UserInfoDto;
import com.project.Teaming.domain.user.dto.response.UserReportCnt;
import com.project.Teaming.domain.user.service.PortfolioService;
import com.project.Teaming.domain.user.service.UserService;
import com.project.Teaming.global.result.ResultCode;
import com.project.Teaming.global.result.ResultDetailResponse;
import com.project.Teaming.global.result.ResultListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 관련 API")
public class UserController {

    private final UserService userService;
    private final MentoringTeamService mentoringTeamService;

    @PostMapping("/user")
    @Operation(summary = "추가 정보 기입", description = "첫 로그인 후 추가 정보 기입할 때(또는 추가 정보 기입이 안되어 있을 때) 사용하는 Api. 닉네임은 필수, 소개와 기술스택은 선택")
    public ResultDetailResponse<Void> addUserInfo(@Valid @RequestBody RegisterDto dto) {
        userService.saveUserInfo(dto);
        return new ResultDetailResponse<>(ResultCode.REGISTER_ADDITIONAL_USER_INFO, null);
    }

    @GetMapping("/user")
    @Operation(summary = "로그인 된 사용자 정보 조회", description = "회원 정보(이메일, 이름, 가입경로, 포트폴리오, 경고누적횟수) 조회하는 Api(AccessToken 기입 필요) " +
            "마이페이지에서 사용 " )
    public ResultDetailResponse<UserInfoDto> getUserInfo() {
        UserInfoDto userInfo = userService.getAuthenticatedUserInfo();
        return new ResultDetailResponse<>(ResultCode.GET_USER_INFO, userInfo);
    }
    @GetMapping("/user/{user_Id}")
    @Operation(summary = "회원 정보 조회", description = "회원 정보(이메일, 이름, 가입경로, 포트폴리오, 경고누적횟수) 조회하는 Api(AccessToken 기입 필요) " +
            "유저페이지에서 사용")
    public ResultDetailResponse<UserInfoDto> getUserInfo(@PathVariable Long user_Id) {
        UserInfoDto userInfoDto = userService.getUserInfo(user_Id);
        return new ResultDetailResponse<>(ResultCode.GET_USER_INFO, userInfoDto);
    }

    @GetMapping("/user/reviews")
    @Operation(summary = "로그인 된 사용자의 리뷰 조회", description = "해당 사용자의 리뷰를 가져온다, 마이페이지에서 사용")
    public ResultListResponse<ReviewDto> getReviews() {
        List<ReviewDto> reviews = userService.getAuthenticatedUserReviews();
        return new ResultListResponse<>(ResultCode.GET_USER_REVIEWS, reviews);
    }

    @GetMapping("/user/{user_id}/reviews")
    @Operation(summary = "회원의 리뷰 조회", description = "해당 회원의 리뷰를 가져온다, 유저페이지에서 사용")
    public ResultListResponse<ReviewDto> getUserReviews(@PathVariable Long user_id) {
        List<ReviewDto> reviews = userService.getReviews(user_id);
        return new ResultListResponse<>(ResultCode.GET_USER_REVIEWS, reviews);
    }

    @GetMapping("/user/my-page/report")
    @Operation(summary = "로그인 된 사용자의 경고 횟수 조회", description = "특정유저의 경고 누적 횟수를 조회하는 API")
    public ResultDetailResponse<UserReportCnt> userReportInfo() {
        UserReportCnt cnt = userService.getWarningCnt();
        return new ResultDetailResponse<>(ResultCode.GET_USER_WARNING_CNT, cnt);
    }

    @PutMapping("/user/update")
    @Operation(summary = "사용자 정보 수정(닉네임, 소개, 기술 스택)", description = "마이페이지에서 사용자의 닉네임과 소개, 기술 스택을 수정할 때 사용하는 API")
    public ResultDetailResponse<Void> updateUser(@Valid @RequestBody UpdateUserInfoDto updateUserInfoDto) {
        userService.updateUser(updateUserInfoDto);
        return new ResultDetailResponse<>(ResultCode.UPDATE_USER_INFO, null);
    }

    @GetMapping("/user/mentoring/teams")
    @Operation(summary = "로그인 된 사용자의 모든 멘토링 팀 조회", description = "나의 모든 멘토링 팀을 조회할 수 있다. 마이페이지에서 사용")
    public ResultListResponse<MyTeamDto> findMyMentoringTeams() {
        List<MentoringTeam> myMentoringTeams = mentoringTeamService.getAuthenticateTeams();
        List<MyTeamDto> teams = myMentoringTeams.stream()
                .map(mentoringTeamService::getMyTeam)
                .collect(Collectors.toList());
        return new ResultListResponse<>(ResultCode.GET_MY_ALL_MENTORING_TEAM, teams);
    }

    @GetMapping("/users/{user_id}/mentoring/teams")
    @Operation(summary = "유저의 모든 멘토링 팀 조회", description = "유저의 모든 멘토링 팀을 조회할 수 있다. 유저페이지에서 사용")
    public ResultListResponse<MyTeamDto> findUserMentoringTeams(@PathVariable Long user_id) {
        List<MentoringTeam> myMentoringTeams = mentoringTeamService.findMyMentoringTeams(user_id);
        List<MyTeamDto> teams = myMentoringTeams.stream()
                .map(mentoringTeamService::getMyTeam)
                .collect(Collectors.toList());
        return new ResultListResponse<>(ResultCode.GET_ALL_USER_MENTORING_TEAM, teams);
    }

}