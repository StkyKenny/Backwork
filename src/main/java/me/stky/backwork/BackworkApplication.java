package me.stky.backwork;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BackworkApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackworkApplication.class, args);
		System.out.println("http://localhost:8187/swagger-ui/index.html");
	}

}
