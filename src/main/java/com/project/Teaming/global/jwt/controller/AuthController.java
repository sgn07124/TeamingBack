package com.project.Teaming.global.jwt.controller;

import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.service.UserService;
import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.exception.BusinessException;
import com.project.Teaming.global.jwt.*;
import com.project.Teaming.global.jwt.dto.LoginRequest;
import com.project.Teaming.global.jwt.dto.LoginResponse;
import com.project.Teaming.global.jwt.dto.StatusResponseDto;
import com.project.Teaming.global.result.ResultCode;
import com.project.Teaming.global.result.ResultDetailResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final RefreshTokenService tokenService;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final RefreshTokenRepository repository;

    @PostMapping("token/logout")
    public ResponseEntity<StatusResponseDto> logout(HttpServletResponse response,
                                                    @CookieValue(value = "accessToken" , required = false) String accessToken,
                                                    @CookieValue(value = "refreshToken", required = false) String refreshToken) {
        if (refreshToken != null) {
            // Redis에서 RefreshToken 정보 삭제
            tokenService.removeRefreshToken(refreshToken);

            // RefreshToken 쿠키 삭제
            ResponseCookie deleteRefreshTokenCookie = ResponseCookie.from("refreshToken", null)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("None")   // 교차 출처 요청 허용
                    .domain("myspringserver.shop")
                    .path("/")
                    .maxAge(0)  // 즉시 만료
                    .build();
            response.addHeader("Set-Cookie", deleteRefreshTokenCookie.toString());
        }
        if (accessToken != null) {
            // AccessToken 쿠키 삭제
            ResponseCookie deleteAccessTokenCookie = ResponseCookie.from("accessToken", null)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("None")   // 교차 출처 요청 허용
                    .domain("myspringserver.shop")
                    .path("/")
                    .maxAge(0)  // 즉시 만료
                    .build();
            response.addHeader("Set-Cookie", deleteAccessTokenCookie.toString());
        }
        return ResponseEntity.ok(StatusResponseDto.addStatus(200));
    }

    @PostMapping("token/refresh")
    public ResponseEntity<TokenResponseStatus> refresh(@CookieValue(value = "refreshToken", required = false) String refreshToken, HttpServletResponse response) {

        if (refreshToken == null) {
            return ResponseEntity.badRequest().body(TokenResponseStatus.addStatus(400, null));
        }

        // RefreshToken 검증
        if (!jwtUtil.verifyToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(TokenResponseStatus.addStatus(401, null));
        }

        // 리프레시 토큰에서 사용자 ID와 권한 추출
        String userEmail = jwtUtil.getUid(refreshToken);

        // Redis에 저장된 RefreshToken 조회
        RefreshToken storedToken = repository.findById(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_IN_REDIS));

        // 요청된 리프레시 토큰과 Redis에 저장된 토큰 비교
        if (!storedToken.getRefreshToken().equals(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(TokenResponseStatus.addStatus(401, null));
        }

        // 새로운 액세스 토큰 생성
        String userRole = jwtUtil.getRole(refreshToken);
        String newAccessToken = jwtUtil.generateAccessToken(userEmail, userRole);
        log.info("새로 발급된 Access Token: {}", newAccessToken);

        // 새로운 AccessToken을 HttpOnly 쿠키로 설정
        ResponseCookie newAccessTokenCookie = ResponseCookie.from("accessToken", newAccessToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")   // 교차 출처 요청 허용
                .domain("myspringserver.shop")
                .path("/")
                .maxAge(1800) // 유효 기간 설정
                .build();
        response.addHeader("Set-Cookie", newAccessTokenCookie.toString());
        tokenService.saveNewAccessTokenInfo(userEmail, newAccessToken);

        // 새로운 액세스 토큰을 반환해준다.
        return ResponseEntity.ok(TokenResponseStatus.addStatus(200, newAccessToken));
    }

    @PostMapping("/login")
    public ResultDetailResponse<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        System.out.println("sdfsd");
        Optional<User> userOptional = userService.findByEmail(request.getEmail());

        if (userOptional.isEmpty()) {
            throw new BusinessException(ErrorCode.USER_NOT_EXIST);
        }

        User user = userOptional.get();
        System.out.println("sdfsdf");

        String role = user.getUserRole();
        GeneratedToken token = jwtUtil.generateToken(request.getEmail(), role);

        return new ResultDetailResponse<>(ResultCode.LOGIN_SUCCESS, new LoginResponse(token.getAccessToken(), token.getRefreshToken()));
    }
}
