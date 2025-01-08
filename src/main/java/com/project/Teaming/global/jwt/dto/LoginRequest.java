package com.project.Teaming.global.jwt.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginRequest {
    @NotNull
    private String email;
}
