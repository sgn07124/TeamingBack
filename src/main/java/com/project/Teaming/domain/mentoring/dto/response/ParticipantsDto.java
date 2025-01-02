package com.project.Teaming.domain.mentoring.dto.response;

import com.project.Teaming.domain.mentoring.entity.MentoringAuthority;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantsDto<T> {

    private MentoringAuthority authority;
    private T details;
}
