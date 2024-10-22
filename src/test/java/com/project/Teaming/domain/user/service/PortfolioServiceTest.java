package com.project.Teaming.domain.user.service;

import com.project.Teaming.domain.user.dto.request.PortfolioDto;
import com.project.Teaming.domain.user.dto.request.RegisterDto;
import com.project.Teaming.domain.user.entity.Portfolio;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class PortfolioServiceTest {

    @Autowired
    PortfolioService portfolioService;
    @Autowired
    UserService userService;

    @Test
    public void findPortfolio() {
        //given
        createUser();
        //when
        Portfolio portfolio = portfolioService.findPortfolio("test@naver.com");
        Portfolio portfolio2 = portfolioService.findPortfolio("test2@naver.com");
        //then
        assertThat(portfolio.getIntroduce()).isNull();
        assertThat(portfolio.getSkills()).isNull();
        assertThat(portfolio2.getIntroduce()).isNull();
        assertThat(portfolio2.getSkills()).isNull();

    }

    @Test
    public void getUpdatePortfolio() {
        //given
        createUser();
        //when
        PortfolioDto portfolioDto = new PortfolioDto();
        portfolioDto.setIntroduce("안녕하세요 테스터입니다");
        portfolioDto.setSkills("스프링,jpa,mysql");
        portfolioService.updatePortfolio("test2@naver.com", portfolioDto);
        PortfolioDto dto = portfolioService.getPortfolio("test@naver.com");
        PortfolioDto dto1 = portfolioService.getPortfolio("test2@naver.com");
        //then
        Assertions.assertThat(dto.getIntroduce()).isNull();
        Assertions.assertThat(dto.getSkills()).isNull();
        Assertions.assertThat(dto1.getSkills()).isEqualTo("스프링,jpa,mysql");
        Assertions.assertThat(dto1.getIntroduce()).isEqualTo("안녕하세요 테스터입니다");
    }



    private void createUser() {
        userService.saveUser("test@naver.com", "testprovider", "ROLE_USER");
        RegisterDto registerDto = new RegisterDto("tester");
        userService.saveUserInfo("test@naver.com", registerDto);

        userService.saveUser("test2@naver.com", "testprovider2", "ROLE_USER");
        RegisterDto registerDto2 = new RegisterDto("tester");
        userService.saveUserInfo("test2@naver.com", registerDto2);
    }
}