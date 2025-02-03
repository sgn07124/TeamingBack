package com.project.Teaming.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

public class AsyncConfig {

    @Bean(name = "threadPoolTaskExecutor")
    public Executor threadPoolTaskExecutor() {
        int core = Runtime.getRuntime().availableProcessors(); // CPU 코어 수
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(core * 2);   // 기본적으로 사용할 스레드 개수
        executor.setMaxPoolSize(core * 4);    // 최대 스레드 개수 증가
        executor.setQueueCapacity(500);      // 작업 대기열 크기 증가
        executor.setKeepAliveSeconds(60);     // 유휴 스레드 유지 시간
        executor.setThreadNamePrefix("AsyncExecutor-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // 거부 정책 설정
        executor.initialize();
        return executor;
    }

}
