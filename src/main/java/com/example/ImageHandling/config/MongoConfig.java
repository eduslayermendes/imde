package com.example.ImageHandling.config;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 3/19/2025
 */
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
public class MongoConfig {


	@Bean
	public MongoTransactionManager transactionManager( MongoDatabaseFactory dbFactory) {
		return new MongoTransactionManager(dbFactory);
	}

	@Bean
	public MongoCustomConversions customConversions() {
		return new MongoCustomConversions( Arrays.asList(
			new StringToLocalDateTimeConverter(),
			new StringToLocalDateConverter()
		));
	}
}

