package com.project.Teaming.domain.project.dto.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class JoinTeamDto {

    @NotNull
    @JsonDeserialize(using = NumberDeserializers.LongDeserializer.class) // String을 Long으로 변환
    private Long teamId;

    @NotNull(message = "모집 카테고리를 입력해주세요. ex) 백엔드")
    private String recruitCategory;
}
