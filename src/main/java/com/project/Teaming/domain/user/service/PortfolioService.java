package com.project.Teaming.domain.user.service;

import com.project.Teaming.domain.user.dto.request.PortfolioDto;
import com.project.Teaming.domain.user.entity.Portfolio;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.repository.PortfolioRepository;
import com.project.Teaming.domain.user.repository.UserRepository;
import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PortfolioService {

    private final UserRepository userRepository;

    public Portfolio findPortfolio(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST));
        return user.getPortfolio();
    }

    public PortfolioDto getPortfolio(String email) {
        Portfolio portfolio = findPortfolio(email);
        PortfolioDto dto = new PortfolioDto();
        dto.setIntroduce(portfolio.getIntroduce());
        return dto;
    }
}
