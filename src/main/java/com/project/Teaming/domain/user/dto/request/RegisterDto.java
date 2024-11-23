package com.project.Teaming.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDto {

    @NotNull(message = "닉네임을 입력해주세요.")
    private String name;
    private String introduce;
    private List<Long> stackIds; // 선택된 기술 스택의 ID 리스트
}
