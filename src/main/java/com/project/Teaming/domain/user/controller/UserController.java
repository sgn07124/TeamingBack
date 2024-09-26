package com.project.Teaming.domain.user.controller;

import com.project.Teaming.domain.user.dto.request.RegisterDto;
import com.project.Teaming.domain.user.dto.response.UserInfoDto;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.service.UserService;
import com.project.Teaming.global.jwt.dto.SecurityUserDto;
import com.project.Teaming.global.jwt.dto.StatusResponseDto;
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
public class UserController {

    private final UserService userService;

    @PostMapping("/user")
    public ResponseEntity<StatusResponseDto> addUserInfo(@RequestBody RegisterDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUserDto securityUser = (SecurityUserDto) authentication.getPrincipal();
        String email = securityUser.getEmail();
        log.info("email : " + email);
        userService.saveUserInfo(email, dto);
        return ResponseEntity.ok(StatusResponseDto.addStatus(200));
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserInfo(UserInfoDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUserDto securityUser = (SecurityUserDto) authentication.getPrincipal();
        User user = userService.findByEmail(securityUser.getEmail()).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없음"));
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setProvider(user.getProvider());
        return ResponseEntity.ok(StatusResponseDto.success(dto));
    }
}

