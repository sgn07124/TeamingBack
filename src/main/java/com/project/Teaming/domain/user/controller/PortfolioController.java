package com.project.Teaming.domain.user.controller;

import com.project.Teaming.domain.user.dto.request.PortfolioDto;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.service.PortfolioService;
import com.project.Teaming.domain.user.service.UserService;
import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.exception.BusinessException;
import com.project.Teaming.global.result.ResultCode;
import com.project.Teaming.global.result.ResultDetailResponse;
import com.project.Teaming.global.result.ResultListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PortfolioController implements SwaggerPortfolioController{

    private final PortfolioService portfolioService;
    private final UserService userService;

    // 수정 필요
    @Override
    @GetMapping("/users/{userId}/portfolios")
    public ResultDetailResponse<PortfolioDto> getUserInfo(@PathVariable Long userId) {
        User user = userService.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST));
        return new ResultDetailResponse<>(ResultCode.GET_USER_PORTFOLIO, portfolioService.getPortfolio(user.getEmail()));

    }
}
