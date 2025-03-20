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
        executor.setCorePoolSize(100);  // 최소 스레드 개수
        executor.setMaxPoolSize(150);  // 최대 스레드 개수
        executor.setQueueCapacity(200);  // 대기 큐 크기
        executor.setThreadNamePrefix("Notification-");
        executor.initialize();
        return executor;
    }
}
