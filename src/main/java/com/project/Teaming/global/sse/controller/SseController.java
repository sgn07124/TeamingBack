package com.project.Teaming.global.sse.controller;

import com.project.Teaming.global.result.ResultCode;
import com.project.Teaming.global.result.ResultDetailResponse;
import com.project.Teaming.global.result.ResultListResponse;
import com.project.Teaming.global.sse.dto.EventPayload;
import com.project.Teaming.global.sse.dto.NotificationResponseDto;
import com.project.Teaming.global.sse.service.NotificationService;
import com.project.Teaming.global.sse.service.SseEmitterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/sse")
@RequiredArgsConstructor
@Tag(name = "SSE 알림", description = "알림 관련 API")
public class SseController {

    private final SseEmitterService sseEmitterService;
    private final NotificationService notificationService;

    @GetMapping(value = "/subscribe", produces = "text/event-stream")
    @Operation(summary = "알림 이벤트 구독", description = "클라이언트의 이벤트 구독을 수락하는 초기 SSE 연결 요청으로 연결을 함으로써 서버에서 클라이언트로 이벤트를 보낼 수 있게 된다.")
    public SseEmitter subscribe() {
        return sseEmitterService.subscribe();
    }

    @PostMapping("/send")
    public void sendNotification(@PathVariable Long userId, @RequestBody EventPayload eventPayload) {
        sseEmitterService.send(userId, eventPayload);
    }

    @GetMapping("/notifications")
    @Operation(summary = "알림 내역 조회", description = "로그인 한 사용자가 수신한 알림 내역을 조회한다.")
    public ResultListResponse<NotificationResponseDto> getNotifications() {
        List<NotificationResponseDto> list = notificationService.getNotifications();
        return new ResultListResponse<>(ResultCode.GET_NOTIFICATIONS, list);
    }

    @DeleteMapping("/notification/{notificationId}")
    @Operation(summary = "알림 삭제", description = "특정 알림을 삭제한다.")
    public ResultDetailResponse<Void> deleteNotification(@PathVariable Long notificationId) {
        notificationService.deleteNotification(notificationId);
        return new ResultDetailResponse<>(ResultCode.DELETE_NOTIFICATION, null);
    }
}
