package com.project.Teaming.global.sse.repository;

import com.project.Teaming.global.sse.entity.Notification;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserId(Long userId, Sort sort);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id IN :ids")
    int markNotificationsAsRead(@Param("ids") List<Long> ids);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.id IN :ids")
    int deleteNotificationsByIds(@Param("ids") List<Long> ids);
}
