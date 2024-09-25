package com.project.Teaming.domain.user.entity;

import com.project.Teaming.domain.mentoring.entity.MentoringParticipation;
import com.project.Teaming.domain.project.entity.ProjectParticipation;
import com.project.Teaming.domain.project.entity.ProjectTeam;
import com.project.Teaming.global.auditing.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userId")
    private Long id;  // 사용자Id

    @Column(name = "userEmail", nullable = false, unique = true, length = 100)
    private String email;  // 사용자 이메일

    @Column(name = "password")
    private String password;  // 사용자 비밀번호

    @Column(name = "userName", nullable = false, length = 50)
    private String name;  // 사용자 이름

    @Column(name = "warningCnt", nullable = false)
    private int warningCnt;  // 사용자가 받은 경고 횟수

    @OneToMany(mappedBy = "user")
    private List<ProjectParticipation> projectParticipations = new ArrayList<>();

    @OneToMany(mappedBy = "reportedUser")
    private List<Report> reports = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolioId")
    private Portfolio portfolio;

    @OneToMany(mappedBy = "user")
    private List<MentoringParticipation> mentoringParticipations = new ArrayList<>();
}
