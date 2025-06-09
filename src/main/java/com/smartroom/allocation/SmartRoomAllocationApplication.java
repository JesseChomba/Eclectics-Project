package com.smartroom.allocation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmartRoomAllocationApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartRoomAllocationApplication.class, args);
	}

}
