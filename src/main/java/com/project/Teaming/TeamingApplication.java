package com.project.Teaming;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class TeamingApplication {


	public static void main(String[] args) {
		SpringApplication.run(TeamingApplication.class, args);
	}

}
