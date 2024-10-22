package com.project.Teaming.domain.user.service;

import com.project.Teaming.domain.user.dto.request.RegisterDto;
import com.project.Teaming.domain.user.entity.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    UserService userService;

    @Test
    void addUser() {
        //given, when
        userService.saveUser("test@naver.com", "testprovider", "ROLE_USER");
        RegisterDto registerDto = new RegisterDto("tester");
        userService.saveUserInfo("test@naver.com", registerDto);

        //then
        User user = userService.findByEmail("test@naver.com").get();
        assertThat(user.getProvider()).isEqualTo("testprovider");
        assertThat(user.getName()).isEqualTo("tester");
        Assertions.assertThat(user.getUserRole()).isEqualTo("ROLE_USER");
    }

    @Test
    void updateUser() {
        //given
        userService.saveUser("test@naver.com", "testprovider", "USER");
        RegisterDto registerDto = new RegisterDto("tester");
        userService.saveUserInfo("test@naver.com", registerDto);

        //when
        RegisterDto dto = new RegisterDto("testerUpdate");
        userService.updateUser("test@naver.com", dto);  //유저 닉네임 변경

        //then
        User user = userService.findByEmail("test@naver.com").get();
        User user1 = userService.findById(1L).get();
        assertThat(user).isSameAs(user1);
        assertThat(user.getName()).isEqualTo("testerUpdate");
        assertThat(user.getWarningCnt()).isEqualTo(0);  //경고횟수 조회

    }

}