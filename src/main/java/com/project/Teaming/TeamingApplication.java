package com.project.Teaming;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
@OpenAPIDefinition(servers = {@Server(url = "https://myspringserver.shop", description = "teaming server")})
public class TeamingApplication {


	public static void main(String[] args) {
		SpringApplication.run(TeamingApplication.class, args);
	}

}
