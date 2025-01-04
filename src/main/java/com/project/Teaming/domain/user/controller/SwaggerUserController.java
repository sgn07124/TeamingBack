package com.project.Teaming.domain.user.controller;

import com.project.Teaming.domain.mentoring.dto.response.MyTeamDto;
import com.project.Teaming.domain.user.dto.request.RegisterDto;
import com.project.Teaming.domain.user.dto.request.UpdateUserInfoDto;
import com.project.Teaming.domain.user.dto.response.ReviewDto;
import com.project.Teaming.domain.user.dto.response.UserInfoDto;
import com.project.Teaming.domain.user.dto.response.UserReportCnt;
import com.project.Teaming.global.result.ResultDetailResponse;
import com.project.Teaming.global.result.ResultListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "User", description = "사용자 관련 API")
public interface SwaggerUserController {

    @Operation(summary = "추가 정보 기입", description = "첫 로그인 후 추가 정보 기입할 때(또는 추가 정보 기입이 안되어 있을 때) 사용하는 Api. 닉네임은 필수, 소개와 기술스택은 선택")
    public ResultDetailResponse<Void> addUserInfo(@Valid @RequestBody RegisterDto dto);

    @Operation(summary = "회원 정보 조회", description = "회원 정보(이메일, 이름, 가입경로, 포트폴리오, 경고누적횟수) 조회하는 Api(AccessToken 기입 필요) " +
            "마이페이지에서 사용 " )
    public ResultDetailResponse<UserInfoDto> getUserInfo();

    @Operation(summary = "회원 정보 조회", description = "회원 정보(이메일, 이름, 가입경로, 포트폴리오, 경고누적횟수) 조회하는 Api(AccessToken 기입 필요) " +
            "유저페이지에서 사용")
    public ResultDetailResponse<UserInfoDto> getUserInfo(@PathVariable Long user_Id);

    @Operation(summary = "로그인 된 사용자의 리뷰 조회", description = "해당 사용자의 리뷰를 가져온다, 마이페이지에서 사용")
    public ResultListResponse<ReviewDto> getReviews();

    @Operation(summary = "회원의 리뷰 조회", description = "해당 회원의 리뷰를 가져온다, 유저페이지에서 사용")
    public ResultListResponse<ReviewDto> getUserReviews(@PathVariable Long user_id);

    @Operation(summary = "로그인 된 사용자의 경고 횟수 조회", description = "특정유저의 경고 누적 횟수를 조회하는 API")
    public ResultDetailResponse<UserReportCnt> userReportInfo();

    @Operation(summary = "사용자 정보 수정(닉네임, 소개, 기술 스택)", description = "마이페이지에서 사용자의 닉네임과 소개, 기술 스택을 수정할 때 사용하는 API")
    public ResultDetailResponse<Void> updateUser(@Valid @RequestBody UpdateUserInfoDto updateUserInfoDto);

    @Operation(summary = "로그인 된 사용자의 모든 멘토링 팀 조회", description = "나의 모든 멘토링 팀을 조회할 수 있다. 마이페이지에서 사용")
    public ResultListResponse<MyTeamDto> findMyMentoringTeams();

    @Operation(summary = "유저의 모든 멘토링 팀 조회", description = "유저의 모든 멘토링 팀을 조회할 수 있다. 유저페이지에서 사용")
    public ResultListResponse<MyTeamDto> findUserMentoringTeams(@PathVariable Long user_id);
}
