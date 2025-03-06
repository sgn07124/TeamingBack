package com.project.Teaming.domain.mentoring.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.Teaming.domain.mentoring.entity.MentoringAuthority;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantsResponse<T> {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long teamId;
    private MentoringAuthority authority;
    private T details;
}
