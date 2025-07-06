package com.example.ImageHandling.domains;

import org.springframework.context.ApplicationEvent;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 9/25/2024
 */
public class RegexPatternDeletedEvent extends ApplicationEvent {

	private final String patternId;

	public RegexPatternDeletedEvent( Object source, String patternId ) {
		super( source );
		this.patternId = patternId;
	}

	public String getPatternId() {
		return patternId;
	}
}
