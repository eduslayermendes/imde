package com.example.ImageHandling.domains.types;

public enum LogAction {
	UPLOAD( "Upload" ), SUBMIT( "Submit" );

	private final String val;

	private LogAction( String val ) {
		this.val = val;
	}

	@Override
	public String toString() {
		return val;
	}
}
