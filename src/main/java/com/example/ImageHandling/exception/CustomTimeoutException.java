package com.example.ImageHandling.exception;

import java.util.concurrent.TimeoutException;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 4/8/2025
 */
public class CustomTimeoutException extends RuntimeException {

	public CustomTimeoutException( String message, TimeoutException e ) {
		super( message );
	}
}
