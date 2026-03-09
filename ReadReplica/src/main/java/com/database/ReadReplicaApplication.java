package com.database;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication
public class ReadReplicaApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReadReplicaApplication.class, args);
	}

}
