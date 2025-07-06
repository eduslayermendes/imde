package com.example.ImageHandling.exception;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 4/7/2025
 */
public class DuplicateInvoiceException extends RuntimeException {

	public DuplicateInvoiceException( String message ) {
		super( message );
	}
}
