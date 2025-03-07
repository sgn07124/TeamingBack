package com.project.Teaming.domain.project.dto.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReviewDto {

    @NotNull
    @JsonDeserialize(using = NumberDeserializers.LongDeserializer.class) // String을 Long으로 변환
    private Long teamId;

    @NotNull(message = "리뷰 대상자 ID를 입력해 주세요.")
    @JsonDeserialize(using = NumberDeserializers.LongDeserializer.class) // String을 Long으로 변환
    private Long revieweeId;  // 리뷰 대상자 id

    @NotNull(message = "1~5로 입력해 주세요.")
    private int rating;

    @NotNull(message = "리뷰 내용을 입력해 주세요.")
    private String content;
}
