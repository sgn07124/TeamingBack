package com.project.Teaming.global.jwt.filter;

import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.repository.UserRepository;
import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.exception.BusinessException;
import com.project.Teaming.global.jwt.JwtUtil;
import com.project.Teaming.global.jwt.dto.SecurityUserDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 쿠키에서 AccessToken을 가져온다.
        String accessToken = resolveTokenFromCookie(request);
        log.info("AccessToken from Cookie: {}", accessToken);

        // AccessToken이 없으면 필터 체인을 그대로 진행한다.
        if (accessToken == null || !jwtUtil.verifyToken(accessToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        // AccessToken이 유효한 경우, 사용자 정보를 조회하고 인증 객체를 설정한다.
        String email = jwtUtil.getUid(accessToken);
        User findUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST));

        // SecurityContext에 등록할 User 객체를 만들어준다.
        SecurityUserDto userDto = SecurityUserDto.builder()
                .userId(findUser.getId())
                .email(findUser.getEmail())
                .role(findUser.getUserRole().startsWith("ROLE_") ? findUser.getUserRole() : "ROLE_" + findUser.getUserRole())  // 중복 방지
                .nickname(findUser.getName())
                .build();

        // SecurityContext에 인증 객체를 등록해준다.
        Authentication auth = getAuthentication(userDto);
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }

    private String resolveTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public Authentication getAuthentication(SecurityUserDto member) {
        return new UsernamePasswordAuthenticationToken(member, "",
                List.of(new SimpleGrantedAuthority(member.getRole())));
    }
}
