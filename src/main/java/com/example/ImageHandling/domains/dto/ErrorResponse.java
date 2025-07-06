package com.example.ImageHandling.domains.dto;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 10/7/2024
 */
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Setter
@Getter
public class ErrorResponse {
	private int status;
	private String error;
	private String message;
	private String timestamp;

	public ErrorResponse(int status, String error, String message) {
		this.status = status;
		this.error = error;
		this.message = message;
		this.timestamp = getCurrentTimestamp();
	}


	private String getCurrentTimestamp() {
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
		return now.atZone( ZoneId.systemDefault()).format(formatter);
	}
}
