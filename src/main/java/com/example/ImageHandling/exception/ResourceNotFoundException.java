package com.example.ImageHandling.exception;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 3/21/2025
 */
public class ResourceNotFoundException extends RuntimeException {
	public ResourceNotFoundException(String message) {
		super(message);
	}
}

