package com.project.Teaming.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableRedisRepositories
@RequiredArgsConstructor
public class RedisConfig {

    private final RedisProperties redisProperties;
    @Value("${spring.data.redis.host}") String host;

    private RedisConnectionFactory redisConnectionFactory(int databaseIndex) {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(redisProperties.getHost());
        redisStandaloneConfiguration.setPort(redisProperties.getPort());
        redisStandaloneConfiguration.setPassword(redisProperties.getPassword());
        redisStandaloneConfiguration.setDatabase(databaseIndex);
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }

    // 기본 db(0번)용 RedisTemplate
    @Bean
    public RedisTemplate<?, ?> redisTemplate() {
        // 0번 DB(기본값) 설정
        // redisTemplate 를 받아와서 set, get, delete 를 사용
        RedisTemplate<byte[], byte[]> redisTemplate = new RedisTemplate<>();
        /*
         * setKeySerializer, setValueSerializer 설정
         * redis-cli 을 통해 직접 데이터를 조회 시 알아볼 수 없는 형태로 출력되는 것을 방지
         */
        redisTemplate.setConnectionFactory(redisConnectionFactory(0));

        return redisTemplate;
    }


    // 게시글 db(1번)용 RedisTemplate
    @Bean
    public RedisTemplate<String, Object> postRedisTemplate() {
        // 1번 DB에서 사용할 RedisTemplate 설정
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory(1));

        // 직렬화
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }

    @Bean(name = "stringRedisTemplate")
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(redisConnectionFactory(2));
    }
}
