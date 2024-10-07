package com.project.Teaming.domain.user.service;

import com.project.Teaming.domain.user.dto.request.PortfolioDto;
import com.project.Teaming.domain.user.entity.Portfolio;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.repository.PortfolioRepository;
import com.project.Teaming.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;

    public Portfolio findPortfolio(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없음"));
        return user.getPortfolio();
    }

    public void savePortfolio(String email, PortfolioDto dto) {
        Portfolio portfolio = findPortfolio(email);
        portfolio.updatePortfolioInfo(dto.getIntroduce(), dto.getSkills());
        portfolioRepository.save(portfolio);
    }

    public PortfolioDto getPortfolio(String email) {
        Portfolio portfolio = findPortfolio(email);
        PortfolioDto dto = new PortfolioDto();
        dto.setIntroduce(portfolio.getIntroduce());
        dto.setSkills(portfolio.getSkills());
        return dto;
    }
}
