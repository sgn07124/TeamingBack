package com.project.Teaming.domain.user.service;

import com.project.Teaming.domain.project.entity.Stack;
import com.project.Teaming.domain.user.dto.request.UpdateUserInfoDto;
import com.project.Teaming.domain.user.dto.response.UserInfoDto;
import com.project.Teaming.domain.user.dto.response.UserReportCnt;
import com.project.Teaming.domain.user.entity.UserStack;
import com.project.Teaming.domain.project.repository.StackRepository;
import com.project.Teaming.domain.user.dto.request.RegisterDto;
import com.project.Teaming.domain.user.entity.Portfolio;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.repository.PortfolioRepository;
import com.project.Teaming.domain.user.repository.UserRepository;
import com.project.Teaming.domain.user.repository.UserStackRepository;
import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.exception.BusinessException;
import com.project.Teaming.global.jwt.dto.SecurityUserDto;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final StackRepository stackRepository;
    private final UserStackRepository userStackRepository;

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


    public void saveUserInfo(RegisterDto dto) {
        SecurityUserDto securityUser = getSecurityUserDto();
        log.info("email : " + securityUser.getEmail());
        User user = findByEmail(securityUser.getEmail()).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST));

        user.updateUserInfo(dto.getName());  // 유저에 닉네임 저장
        Portfolio portfolio = new Portfolio();
        if (dto.getIntroduce() != null && !dto.getIntroduce().trim().isEmpty()) {
            user.linkPortfolio(portfolio, dto);  // 포트폴리오에 자기소개 저장
        }
        if (dto.getStackIds() != null && !dto.getStackIds().isEmpty()) {
            saveUserStacks(dto, portfolio);  // 포트폴리오에 기술스택 저장
        }

        portfolioRepository.save(portfolio);
        userRepository.save(user);
    }

    private void saveUserStacks(RegisterDto dto, Portfolio portfolio) {
        List<Long> stackIds = dto.getStackIds();
        List<Stack> stacks = stackRepository.findAllById(stackIds);

        // 누락된 기술 스택 ID 검증
        List<Long> missingStackIds = stackIds.stream()
                .filter(id -> stacks.stream().noneMatch(stack -> stack.getId().equals(id)))
                .collect(Collectors.toList());
        if (!missingStackIds.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_VALID_STACK_ID);
        }

        for (Stack stack : stacks) {
            UserStack userStack = UserStack.addStacks(portfolio, stack);
            userStackRepository.save(userStack);
        }
    }

    @Transactional
    public void updateUser(UpdateUserInfoDto dto) {
        User user = userRepository.findByEmail(getSecurityUserDto().getEmail()).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST));
        Portfolio portfolio = portfolioRepository.findById(user.getPortfolio().getId()).orElseThrow(() -> new BusinessException(ErrorCode.PORTFOLIO_NOT_EXIST));

        user.updateUserInfo(dto.getName());
        portfolio.updatePortfolioInfo(dto.getIntroduce());

        List<Stack> stacks = stackRepository.findAllById(dto.getStackIds());
        List<Long> missingStackIds = dto.getStackIds().stream()
                .filter(id -> stacks.stream().noneMatch(stack -> stack.getId().equals(id)))
                .collect(Collectors.toList());
        if (!missingStackIds.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_VALID_STACK_ID);
        }

        portfolio.updateStacks(stacks);
    }

    public UserInfoDto getUserInfo(UserInfoDto dto) {
        SecurityUserDto securityUser = getSecurityUserDto();
        User user = findByEmail(securityUser.getEmail()).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST));

        Portfolio portfolio = portfolioRepository.findById(user.getPortfolio().getId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.PORTFOLIO_NOT_EXIST));

        // 기술 스택 이름 리스트 생성
        List<String> stackNames = portfolio.getUserStacks().stream()
                .map(userStack -> userStack.getStack().getStackName())
                .collect(Collectors.toList());

        dto.setUserInfoDto(user, portfolio, stackNames);
        return dto;
    }

    public UserReportCnt getWarningCnt() {
        User user = findByEmail(getSecurityUserDto().getEmail()).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST));
        UserReportCnt userReportCnt = new UserReportCnt();
        userReportCnt.setReportCnt(user.getWarningCnt());
        return userReportCnt;
    }

    private static SecurityUserDto getSecurityUserDto() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUserDto securityUser = (SecurityUserDto) authentication.getPrincipal();
        return securityUser;
    }
}
