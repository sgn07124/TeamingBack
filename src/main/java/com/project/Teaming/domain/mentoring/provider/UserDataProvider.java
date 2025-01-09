package com.project.Teaming.domain.mentoring.provider;

import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.repository.UserRepository;
import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.exception.BusinessException;
import com.project.Teaming.global.jwt.dto.SecurityUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserDataProvider {

    private final UserRepository userRepository;

    public User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUserDto securityUser = (SecurityUserDto) authentication.getPrincipal();
        Long userId = securityUser.getUserId();
        return userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST));
    }

    /**
     * 로그인 하지 않은 사용자면 null반환
     * @return
     */
    public User getOptionalUser() {
        try {
            return getUser(); // getUser() 호출
        } catch (Exception e) {
            return null; // 로그인하지 않은 경우 null 반환
        }
    }

    public User findUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST));
    }
}
