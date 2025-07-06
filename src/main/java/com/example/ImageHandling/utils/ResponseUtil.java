package com.example.ImageHandling.utils;

import com.example.ImageHandling.domains.dto.ErrorResponse;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.nio.file.AccessDeniedException;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 2/14/2025
 */
public final class ResponseUtil {
	private static final Logger logger = LoggerFactory.getLogger(ResponseUtil.class);


	private ResponseUtil() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}

	public static ResponseEntity<ErrorResponse> createErrorResponse(Exception ex, HttpStatus status, String errorTitle) {
		String rootCauseMessage = ExceptionUtils.getRootCauseMessage( ex );
		ErrorResponse errorResponse = new ErrorResponse(
			status.value(),
			errorTitle,
			rootCauseMessage
		);

		return new ResponseEntity<>(errorResponse, status);
	}


}

