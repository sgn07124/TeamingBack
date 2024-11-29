package com.project.Teaming.domain.mentoring.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class BoardResponseDto {

    private List<Long> teamId;
    private RsSpecBoardDto dto;
}