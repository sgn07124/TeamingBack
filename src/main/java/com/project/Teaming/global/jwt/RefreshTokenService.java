package com.project.Teaming.global.jwt;

import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository repository;

    @Transactional
    public void saveTokenInfo(String email, String refreshToken, String accessToken) {
        repository.save(new RefreshToken(email, accessToken, refreshToken));
    }

    @Transactional
    public void saveNewAccessTokenInfo(String email, String accessToken) {
        // 이메일을 기준으로 RefreshToken 조회
        RefreshToken token = repository.findById(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_IN_REDIS));

        // AccessToken 갱신
        token.updateAccessToken(accessToken);

        // 변경된 엔티티를 저장 (갱신)
        repository.save(token);
    }

    @Transactional
    public void removeRefreshToken(String refreshToken) {
        RefreshToken token = repository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_IN_REDIS));

        repository.delete(token);
    }
}
