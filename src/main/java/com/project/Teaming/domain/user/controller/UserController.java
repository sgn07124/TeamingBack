package com.project.Teaming.domain.user.controller;

import com.project.Teaming.domain.mentoring.dto.response.TeamInfoResponse;
import com.project.Teaming.domain.mentoring.entity.MentoringTeam;
import com.project.Teaming.domain.mentoring.provider.UserDataProvider;
import com.project.Teaming.domain.mentoring.service.MentoringTeamService;
import com.project.Teaming.domain.user.dto.request.RegisterDto;
import com.project.Teaming.domain.user.dto.request.UpdateUserInfoDto;
import com.project.Teaming.domain.user.dto.response.ReviewDto;
import com.project.Teaming.domain.user.dto.response.UserInfoDto;
import com.project.Teaming.domain.user.dto.response.UserReportCnt;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.service.UserService;
import com.project.Teaming.global.result.ResultCode;
import com.project.Teaming.global.result.ResultDetailResponse;
import com.project.Teaming.global.result.ResultListResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController implements SwaggerUserController{

    private final UserService userService;
    private final UserDataProvider userDataProvider;
    private final MentoringTeamService mentoringTeamService;

    @Override
    @PostMapping
    public ResultDetailResponse<Void> addUserInfo(@Valid @RequestBody RegisterDto dto) {
        userService.saveUserInfo(dto);
        return new ResultDetailResponse<>(ResultCode.REGISTER_ADDITIONAL_USER_INFO, null);
    }
    @Override
    @GetMapping
    public ResultDetailResponse<UserInfoDto> getUserInfo() {
        return new ResultDetailResponse<>(ResultCode.GET_USER_INFO, userService.getAuthenticatedUserInfo());
    }
    @Override
    @GetMapping("/{userId}")
    public ResultDetailResponse<UserInfoDto> getUserInfo(@PathVariable Long userId) {
        return new ResultDetailResponse<>(ResultCode.GET_USER_INFO, userService.getUserInfo(userId));
    }
    @Override
    @GetMapping("/reviews")
    public ResultListResponse<ReviewDto> getReviews() {
        return new ResultListResponse<>(ResultCode.GET_USER_REVIEWS, userService.getAuthenticatedUserReviews());
    }
    @Override
    @GetMapping("/{userId}/reviews")
    public ResultListResponse<ReviewDto> getUserReviews(@PathVariable Long userId) {
        return new ResultListResponse<>(ResultCode.GET_USER_REVIEWS, userService.getReviews(userId));
    }
    @Override
    @GetMapping("/my-page/report")
    public ResultDetailResponse<UserReportCnt> userReportInfo() {
        return new ResultDetailResponse<>(ResultCode.GET_USER_WARNING_CNT, userService.getWarningCnt());
    }
    @Override
    @PutMapping
    public ResultDetailResponse<Void> updateUser(@Valid @RequestBody UpdateUserInfoDto updateUserInfoDto) {
        userService.updateUser(updateUserInfoDto);
        return new ResultDetailResponse<>(ResultCode.UPDATE_USER_INFO, null);
    }
    @Override
    @GetMapping("/mentoring/teams")
    public ResultListResponse<TeamInfoResponse> findMyMentoringTeams() {
        List<MentoringTeam> myMentoringTeams = mentoringTeamService.getAuthenticateTeams();
        return new ResultListResponse<>(ResultCode.GET_MY_ALL_MENTORING_TEAM,
                myMentoringTeams.stream()
                        .map(mentoringTeamService::getMyTeam)
                        .collect(Collectors.toList()));
    }
    @Override
    @GetMapping("/{userId}/mentoring/teams")
    public ResultListResponse<TeamInfoResponse> findUserMentoringTeams(@PathVariable Long userId) {
        User targetUser = userDataProvider.findUser(userId);
        List<MentoringTeam> myMentoringTeams = mentoringTeamService.findMyMentoringTeams(userId);
        return new ResultListResponse<>(ResultCode.GET_ALL_USER_MENTORING_TEAM,
                myMentoringTeams.stream()
                        .map(team -> mentoringTeamService.getTeamInfoWithAuthority(team, targetUser))
                        .collect(Collectors.toList()));
    }

}