package com.example.ImageHandling;

import com.example.ImageHandling.config.LibraryLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableMongoAuditing
@SpringBootApplication
@EnableTransactionManagement
public class ImageHandlingApplication {

	public static void main(String[] args) {
		SpringApplication.run(ImageHandlingApplication.class, args);
		LibraryLoader.loadLibrary("opencv_java490.dll");
		System.setProperty("TESSDATA_PREFIX", "src/main/resources/");
	}



}
