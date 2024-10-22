package com.project.Teaming.domain.user.dto.response;

import com.project.Teaming.domain.user.dto.request.PortfolioDto;
import com.project.Teaming.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoDto {
    private String email;
    private String name;
    private String provider;
    private PortfolioDto portfolioDto;
    private int warningCnt;

    public void toDto(User user) {
        this.email = user.getEmail();
        this.name = user.getName();
        this.provider = user.getProvider();
        this.warningCnt = user.getWarningCnt();
    }
}
