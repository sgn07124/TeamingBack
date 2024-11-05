package com.project.Teaming.global.jwt.controller;

import com.project.Teaming.global.jwt.*;
import com.project.Teaming.global.jwt.dto.StatusResponseDto;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final RefreshTokenRepository tokenRepository;
    private final RefreshTokenService tokenService;
    private final JwtUtil jwtUtil;

    @PostMapping("token/logout")
    public ResponseEntity<StatusResponseDto> logout(HttpServletResponse response, @CookieValue(value = "accessToken" , required = false) String accessToken) {
        if (accessToken != null) {
            // Redis에서 RefreshToken 정보 삭제
            tokenService.removeRefreshToken(accessToken);

            // AccessToken 쿠키 삭제 설정
            ResponseCookie deleteCookie = ResponseCookie.from("accessToken", null)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("None")
                    .path("/")
                    .maxAge(0)  // 즉시 만료
                    .build();
            response.addHeader("Set-Cookie", deleteCookie.toString());
        }
        return ResponseEntity.ok(StatusResponseDto.addStatus(200));
    }

    @PostMapping("token/refresh")
    public ResponseEntity<TokenResponseStatus> refresh(@CookieValue(value = "accessToken", required = false) String accessToken, HttpServletResponse response) {

        if (accessToken == null) {
            return ResponseEntity.badRequest().body(TokenResponseStatus.addStatus(400, null));
        }
        // 액세스 토큰으로 Refresh 토큰 객체를 조회
        Optional<RefreshToken> refreshToken = tokenRepository.findByAccessToken(accessToken);
        log.info("Access Token from Cookie: {}", accessToken);

        // RefreshToken이 존재하고 유효하다면 실행
        if (refreshToken.isPresent() && jwtUtil.verifyToken(refreshToken.get().getRefreshToken())) {
            // RefreshToken 객체를 꺼내온다.
            RefreshToken resultToken = refreshToken.get();

            // 권한과 아이디를 추출해 새로운 액세스토큰을 만든다.
            String newAccessToken = jwtUtil.generateAccessToken(resultToken.getId(), jwtUtil.getRole(resultToken.getRefreshToken()));
            log.info("accessToken={}", newAccessToken);

            // 액세스 토큰의 값을 수정해준다.
            resultToken.updateAccessToken(newAccessToken);
            tokenRepository.save(resultToken);

            // 새로운 AccessToken을 HttpOnly 쿠키로 설정
            ResponseCookie newAccessTokenCookie = ResponseCookie.from("accessToken", newAccessToken)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("None")
                    .path("/")
                    .maxAge(1800) // 유효 기간 설정
                    .build();
            response.addHeader("Set-Cookie", newAccessTokenCookie.toString());

            // 새로운 액세스 토큰을 반환해준다.
            return ResponseEntity.ok(TokenResponseStatus.addStatus(200, newAccessToken));
        }
        return ResponseEntity.badRequest().body(TokenResponseStatus.addStatus(400, null));
    }
}
