package com.project.Teaming.global.messageQueue.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {

    @Value("${server.id}") // application.yml에서 설정된 SERVER_ID 값을 주입
    private String serverId;
    @Bean
    public Queue queue() {
        return new Queue(serverId, true);
    }
    @Bean
    public Binding binding(FanoutExchange exchange, Queue queue) {
        return BindingBuilder.bind(queue).to(exchange);
    }
    @Bean
    public FanoutExchange exchange() {
        return new FanoutExchange("notification.exchange");
    }
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(CachingConnectionFactory cachingConnectionFactory) {
        return new RabbitAdmin(cachingConnectionFactory);
    }

    @Bean
    public ApplicationRunner runner(RabbitAdmin rabbitAdmin) {
        return args -> rabbitAdmin.initialize();
    }
    @Bean
    public RabbitTemplate rabbitTemplate(CachingConnectionFactory cachingConnectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(cachingConnectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter()); // ✅ JSON 메시지 변환기 적용
        return rabbitTemplate;
    }

    @Bean
    public CachingConnectionFactory cachingConnectionFactory() {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory("localhost");
        cachingConnectionFactory.setUsername("admin");
        cachingConnectionFactory.setPassword("password");
        cachingConnectionFactory.setChannelCacheSize(50);
        cachingConnectionFactory.setCacheMode(CachingConnectionFactory.CacheMode.CHANNEL);
        return cachingConnectionFactory;
    }
}


