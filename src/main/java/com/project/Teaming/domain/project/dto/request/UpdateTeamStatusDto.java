package com.project.Teaming.domain.project.dto.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;
import com.project.Teaming.domain.project.entity.ProjectStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateTeamStatusDto {

    @NotNull
    @JsonDeserialize(using = NumberDeserializers.LongDeserializer.class) // String을 Long으로 변환
    private Long teamId;
    @NotNull(message = "팀 상태를 입력해 주세요. ex) RECRUITING, WORKING, COMPLETE")
    private ProjectStatus status;
}
