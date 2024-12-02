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
@Tag(name = "Portfolio", description = "사용자 정보 관련 API")
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final UserService userService;

    // 수정 필요
    @GetMapping("/user/{id}/portfolio")
    @Operation(summary = "특정 사용자 정보 조회", description = "특정 사용자의 정보를 조회 할 수 있다.")
    public ResultDetailResponse<PortfolioDto> getUserInfo(@PathVariable Long id) {
        User user = userService.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST));
        PortfolioDto dto = portfolioService.getPortfolio(user.getEmail());
        return new ResultDetailResponse<>(ResultCode.GET_USER_PORTFOLIO, dto);

    }
}
