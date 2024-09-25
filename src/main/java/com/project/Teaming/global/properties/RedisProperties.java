package com.project.Teaming.global.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("spring.redis")
public class RedisProperties {

    private String host;
    private int port;
    private String password;
}
