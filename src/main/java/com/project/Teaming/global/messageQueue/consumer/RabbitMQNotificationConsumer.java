package com.project.Teaming.global.messageQueue.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.Teaming.global.sse.dto.EventPayload;
import com.project.Teaming.global.sse.dto.EventWithTeamPayload;
import com.project.Teaming.global.sse.entity.Notification;
import com.project.Teaming.global.sse.service.SseEmitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQNotificationConsumer {

    private final SseEmitterService sseEmitterService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "#{@environment.getProperty('server.id')}", concurrency = "3")
    public void receiveNotification(String messageBody) {
        try {
            Object payload = determinePayloadType(messageBody);
            if (payload == null) return;

            log.info("✅ RabbitMQ 알림 이벤트 수신: {}", payload);
            sendNotification(payload);
        } catch (Exception e) {
            log.error("❌ RabbitMQ 알림 이벤트 처리 실패: {}", e.getMessage(), e);
        }
    }

    private Object determinePayloadType(String messageBody) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(messageBody);
        return jsonNode.has("teamId")
                ? objectMapper.readValue(messageBody, EventWithTeamPayload.class)
                : objectMapper.readValue(messageBody, EventPayload.class);
    }

    private void sendNotification(Object payload) {
        try {
            if (payload instanceof EventWithTeamPayload eventWithTeamPayload) {
                sseEmitterService.sendWithTeamId(eventWithTeamPayload.getUserId(), eventWithTeamPayload);
            } else if (payload instanceof EventPayload eventPayload) {
                sseEmitterService.send(eventPayload.getUserId(), eventPayload);
            }
        } catch (Exception e) {
            log.error("❌ SSE 알림 전송 실패 : {}", e.getMessage(), e);
        }
    }
}
