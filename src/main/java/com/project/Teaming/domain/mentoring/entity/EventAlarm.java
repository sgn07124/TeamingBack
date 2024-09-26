package com.project.Teaming.domain.mentoring.entity;

import com.project.Teaming.domain.project.entity.AlarmStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "event_alarm")
@NoArgsConstructor
@AllArgsConstructor
public class EventAlarm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_alarm_id")
    private Long id;  // 일정 알림 ID

    @Column(name = "time_set", nullable = false)
    private int timeSet;  // 알림 설정 시간

    @Column(name = "is_sent")
    @Enumerated(EnumType.STRING)
    private AlarmStatus isSent;  // 전송 여부

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;  // 알림 내용

    // 외래키 : 멘토링팀 ID, 일정 ID
    @OneToOne
    @JoinColumns({
            @JoinColumn(name = "event_id", referencedColumnName = "event_id"),
            @JoinColumn(name = "mentoring_team_id", referencedColumnName = "mentoring_team_id")
    })
    private Event event;
}
