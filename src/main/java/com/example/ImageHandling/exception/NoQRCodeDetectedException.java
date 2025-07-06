package com.example.ImageHandling.exception;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 10/9/2024
 */
public class NoQRCodeDetectedException extends RuntimeException {

	private static final String DEFAULT_MESSAGE = "No QR Code Detected";

	public NoQRCodeDetectedException() {
		super(DEFAULT_MESSAGE);
	}

	public NoQRCodeDetectedException(String message) {
		super(message);
	}
}

