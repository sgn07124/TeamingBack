package com.project.Teaming.domain.project.entity;

import com.project.Teaming.global.auditing.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "ProjectEventAlarm")
@NoArgsConstructor
@AllArgsConstructor
public class Alarm extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "eventAlarmId")
    private Long id;  // 일정 알람 ID
    @Column(name = "timeSet", nullable = false)
    private int timeSet;  // 알림 설정 시간
    @Column(name = "isSent")
    @Enumerated(EnumType.STRING)
    private AlarmStatus isSent;  // 전송 여부
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;  // 알림 내용
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parId")
    private ProjectParticipation projectParticipation;  // 주인
}