package com.project.Teaming.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;


@EnableAsync
@Configuration
public class AsyncConfig {

    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(50);  // 최소 스레드 개수
        executor.setMaxPoolSize(300);  // 최대 스레드 개수
        executor.setQueueCapacity(3000);  // 대기 큐 크기
        executor.setKeepAliveSeconds(30);  // Idle 상태의 스레드를 유지하는 시간
        executor.setThreadNamePrefix("Notification-");
        executor.initialize();
        return executor;
    }
}
