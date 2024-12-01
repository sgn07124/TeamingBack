package com.project.Teaming.domain.user.controller;

import com.project.Teaming.domain.user.dto.request.RegisterDto;
import com.project.Teaming.domain.user.dto.request.UpdateUserInfoDto;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 관련 API")
public class UserController {

    private final UserService userService;
    private final PortfolioService portfolioService;

    @PostMapping("/user")
    @Operation(summary = "추가 정보 기입", description = "첫 로그인 후 추가 정보 기입할 때(또는 추가 정보 기입이 안되어 있을 때) 사용하는 Api. 닉네임은 필수, 소개와 기술스택은 선택")
    public ResultDetailResponse<Void> addUserInfo(@Valid @RequestBody RegisterDto dto) {
        userService.saveUserInfo(dto);
        return new ResultDetailResponse<>(ResultCode.REGISTER_ADDITIONAL_USER_INFO, null);
    }

    @GetMapping("/user")
    @Operation(summary = "회원 정보 조회", description = "회원 정보(이메일, 이름, 가입경로, 포트폴리오, 경고누적횟수) 조회하는 Api(AccessToken 기입 필요)")
    public ResultDetailResponse<UserInfoDto> getUserInfo() {
        UserInfoDto dto = new UserInfoDto();
        UserInfoDto userInfoDto = userService.getUserInfo(dto);
        return new ResultDetailResponse<>(ResultCode.GET_USER_INFO, userInfoDto);
    }

    @GetMapping("/user/report")
    @Operation(summary = "사용자의 경고 횟수 조회", description = "특정유저의 경고 누적 횟수를 조회하는 API")
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
}