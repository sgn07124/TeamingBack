package com.project.Teaming.domain.user.controller;

import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import com.project.Teaming.domain.user.dto.request.PortfolioDto;
import com.project.Teaming.domain.user.dto.request.RegisterDto;
import com.project.Teaming.domain.user.dto.response.UserInfoDto;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.service.PortfolioService;
import com.project.Teaming.domain.user.service.UserService;
import com.project.Teaming.global.jwt.dto.SecurityUserDto;
import com.project.Teaming.global.jwt.dto.StatusResponseDto;
import com.project.Teaming.global.result.ResultCode;
import com.project.Teaming.global.result.ResultResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.results.ResultBuilderEmbeddable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    @Operation(summary = "추가 정보 기입", description = "첫 로그인 후 추가 정보 기입할 때(또는 추가 정보 기입이 안되어 있을 때) 사용하는 Api")
    public ResponseEntity<StatusResponseDto> addUserInfo(@RequestBody RegisterDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUserDto securityUser = (SecurityUserDto) authentication.getPrincipal();
        String email = securityUser.getEmail();
        log.info("email : " + email);
        userService.saveUserInfo(email, dto);
        return ResponseEntity.ok(StatusResponseDto.addStatus(200));
    }

    @GetMapping("/user")
    @Operation(summary = "회원 정보 조회", description = "회원 정보(이메일, 이름, 가입경로, 포트폴리오, 경고누적횟수) 조회하는 Api(AccessToken 기입 필요)")
    public ResponseEntity<?> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUserDto securityUser = (SecurityUserDto) authentication.getPrincipal();
        UserInfoDto dto = new UserInfoDto();
        User user = userService.findByEmail(securityUser.getEmail()).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없음"));
        dto.toDto(user);
        dto.setPortfolioDto(portfolioService.getPortfolio(securityUser.getEmail()));
        return ResponseEntity.ok(StatusResponseDto.success(dto));
    }

    @GetMapping("/user/{id}/report")
    @Operation(summary = "사용자의 경고 횟수 조회", description = "특정유저의 경고 누적 횟수를 조회하는 API")
    public ResultResponse<Integer> userReportInfo(@PathVariable Long id) {
        User user = userService.findById(id).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없음"));
        int cnt = user.getWarningCnt();
        log.info("유저id = {}, 유저의 경고누적횟수 = {}", id, cnt);
        return new ResultResponse<>(ResultCode.GET_USER_WARNING_CNT, List.of(cnt));
    }

    @PutMapping("/user")
    @Operation(summary = "사용자 정보 업데이트", description = "사용자 닉네임 정보를 수정할 때 사용하는 API")
    public ResultResponse<UserInfoDto> updateUser(@RequestBody RegisterDto registerDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUserDto securityUser = (SecurityUserDto) authentication.getPrincipal();
        User user = userService.findByEmail(securityUser.getEmail()).orElseThrow(() -> new UsernameNotFoundException("업데이트 할 사용자를 찾을 수 없음"));
        userService.updateUser(user.getEmail(),registerDto);
        UserInfoDto dto = new UserInfoDto();
        dto.toDto(user);
        dto.setPortfolioDto(portfolioService.getPortfolio(securityUser.getEmail()));
        return new ResultResponse<>(ResultCode.UPDATE_USER_NICKNAME, List.of(dto));
    }
}

