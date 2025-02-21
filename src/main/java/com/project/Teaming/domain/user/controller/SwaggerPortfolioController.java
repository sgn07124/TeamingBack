package com.project.Teaming.domain.user.controller;

import com.project.Teaming.domain.user.dto.request.PortfolioDto;
import com.project.Teaming.global.result.ResultDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Portfolio", description = "사용자 정보 관련 API")
public interface SwaggerPortfolioController {

    @Operation(summary = "특정 사용자 정보 조회", description = "특정 사용자의 정보를 조회 할 수 있다.")
    public ResultDetailResponse<PortfolioDto> getUserInfo(@PathVariable Long id);
}
