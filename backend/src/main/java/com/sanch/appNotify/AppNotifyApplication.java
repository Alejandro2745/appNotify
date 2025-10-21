package com.sanch.appNotify;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRabbit
public class AppNotifyApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppNotifyApplication.class, args);
	}

}
