package com.project.Teaming.global.event;

import com.project.Teaming.global.messageQueue.publisher.RabbitMQNotificationPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final RabbitMQNotificationPublisher rabbitMQNotificationPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendNotificationsAfterTransaction(NotificationEvent event) {
        log.info("✅ 트랜잭션 종료 후 RabbitMQ 알림 이벤트 발행: {}", event.getNotificationIds());

        // 트랜잭션이 성공적으로 커밋된 후 RabbitMQ로 이벤트 발행
        rabbitMQNotificationPublisher.sendNotificationEvent(event);
    }
}
