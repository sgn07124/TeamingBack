package com.project.Teaming.global.sse.entity;

import com.project.Teaming.domain.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String message;

    private Long teamId;

    private String type;  // 알림 유형

    private boolean isRead; // 읽음 여부

    private LocalDateTime createdAt = LocalDateTime.now();

    public Notification(User user, String message, String type) {
        this.user = user;
        this.message = message;
        this.type = type;
        this.isRead = false;
    }

    public Notification(User user, String message, Long teamId, String type) {
        this.user = user;
        this.message = message;
        this.teamId = teamId;
        this.type = type;
        this.isRead = false;
    }
}
