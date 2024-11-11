package com.project.Teaming.domain.user.service;

import com.project.Teaming.domain.user.dto.request.PortfolioDto;
import com.project.Teaming.domain.user.dto.request.RegisterDto;
import com.project.Teaming.domain.user.entity.Portfolio;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.repository.PortfolioRepository;
import com.project.Teaming.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public void saveUser(String email, String provider, String role) {
        User user = new User(email, provider, role);
        userRepository.save(user);
    }

    @Transactional
    public void saveUserInfo(String email, RegisterDto dto) {
        User user = findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다 : " + email));

        user.updateUserInfo(dto.getName());
        Portfolio portfolio = new Portfolio();
        user.linkPortfolio(portfolio);

        portfolioRepository.save(portfolio);
        userRepository.save(user);
    }

    @Transactional
    public void updateUser(String email, RegisterDto dto) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("업데이트 할 사용자를 찾을 수 없습니다 : " + email));
        user.updateUserInfo(dto.getName());
    }


}
