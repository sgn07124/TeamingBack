package com.project.Teaming.domain.user.controller;

import com.project.Teaming.domain.user.dto.request.RegisterDto;
import com.project.Teaming.domain.user.dto.response.UserInfoDto;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.service.UserService;
import com.project.Teaming.global.jwt.dto.SecurityUserDto;
import com.project.Teaming.global.jwt.dto.StatusResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 관련 API")
public class UserController {

    private final UserService userService;

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
    @Operation(summary = "회원 정보 조회", description = "회원 정보(이메일, 이름, 가입경로) 조회하는 Api(AccessToken 기입 필요)")
    public ResponseEntity<?> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUserDto securityUser = (SecurityUserDto) authentication.getPrincipal();
        UserInfoDto dto = new UserInfoDto();
        User user = userService.findByEmail(securityUser.getEmail()).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없음"));
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setProvider(user.getProvider());
        return ResponseEntity.ok(StatusResponseDto.success(dto));
    }
}

