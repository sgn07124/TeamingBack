package com.project.Teaming.domain.user.service;

import com.project.Teaming.domain.user.dto.request.RegisterDto;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void saveUser(String email, String provider, String role) {
        User user = new User(email, provider, role);
        userRepository.save(user);
    }

    public void saveUserInfo(String email, RegisterDto dto) {
        User user = findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다 : " + email));

        user.updateUserInfo(dto.getName());
        userRepository.save(user);
    }


}
