package com.project.Teaming.domain.user.repository;

import com.project.Teaming.domain.user.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select u from User u " +
    " join fetch u.mentoringParticipations mp " +
    "join fetch mp.mentoringTeam mt " +
    "where u.id = :userId")
    Optional<User> findByIdWithMentoringTeams(@Param("userId") Long userId);

    Optional<User> findByEmail(String email);

}
