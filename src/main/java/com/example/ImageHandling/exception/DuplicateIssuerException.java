package com.example.ImageHandling.exception;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 10/16/2024
 */
public class DuplicateIssuerException extends RuntimeException {
	public DuplicateIssuerException(String message) {
		super(message);
	}
}

