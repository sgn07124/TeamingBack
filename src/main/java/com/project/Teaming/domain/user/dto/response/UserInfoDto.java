package com.project.Teaming.domain.user.dto.response;

import com.project.Teaming.domain.user.dto.request.PortfolioDto;
import com.project.Teaming.domain.user.entity.Portfolio;
import com.project.Teaming.domain.user.entity.User;
import java.util.List;
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
    private int warningCnt;

    private String introduce;
    private List<String> stacks;  // 기술 스택(문자열id로)

    public void setUserInfoDto(User user, Portfolio portfolio, List<String> stackIds) {
        this.email = user.getEmail();
        this.name = user.getName();
        this.provider = user.getProvider();
        this.warningCnt = user.getWarningCount();

        this.introduce = portfolio.getIntroduce();
        this.stacks = stackIds;
    }
}
