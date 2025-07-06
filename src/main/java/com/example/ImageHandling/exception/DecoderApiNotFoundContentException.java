package com.example.ImageHandling.exception;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 10/9/2024
 */
public class DecoderApiNotFoundContentException extends RuntimeException {

	private static final String NOT_FOUND_CONTENT= "Decoder API did not find any content";

	public DecoderApiNotFoundContentException() {
		super( NOT_FOUND_CONTENT );
	}
	public DecoderApiNotFoundContentException(String message) {
		super( String.format( message +" ,{} " , NOT_FOUND_CONTENT));
	}

}
