package com.example.ImageHandling.utils;

import com.example.ImageHandling.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.Principal;
import java.util.Optional;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 2/24/2025
 */
@Configuration
@RequiredArgsConstructor
public class AuditorAwareImpl implements AuditorAware<String> {

	private final AuthService authService;

	@Override
	public Optional<String> getCurrentAuditor() {
		return Optional.ofNullable( SecurityContextHolder.getContext().getAuthentication())
			.map( authentication -> authService.getLoggedInUserDetails().getUsername() );
	}

}
