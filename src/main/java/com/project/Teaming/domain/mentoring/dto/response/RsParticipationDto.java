package com.project.Teaming.domain.mentoring.dto.response;

import com.project.Teaming.domain.mentoring.entity.MentoringAuthority;
import com.project.Teaming.domain.mentoring.entity.MentoringRole;
import lombok.Builder;
import lombok.Data;

@Data
public class RsParticipationDto {
    private Long id;  //id
    private MentoringRole role;  // 역할
    private MentoringAuthority authority;

    @Builder
    public RsParticipationDto(Long id, MentoringRole role, MentoringAuthority authority) {
        this.id = id;
        this.role = role;
        this.authority = authority;
    }
}
