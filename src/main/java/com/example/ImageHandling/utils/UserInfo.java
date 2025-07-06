package com.example.ImageHandling.utils;

import com.example.ImageHandling.services.AuthService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

//TODO: must be deleted after final tests
@Slf4j
public class UserInfo {

	private String username;
	private String email;
	private Map<String, Object> customAttributes;
	private static final Logger logger = LoggerFactory.getLogger( UserInfo.class);


	public UserInfo() throws JsonProcessingException {
		KeycloakAuthenticationToken authentication =
			(KeycloakAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

		if (authentication != null) {
			@SuppressWarnings("unchecked")
			KeycloakPrincipal<KeycloakSecurityContext> principal =
				(KeycloakPrincipal<KeycloakSecurityContext>) authentication.getPrincipal();

			KeycloakSecurityContext session = principal.getKeycloakSecurityContext();

			this.username = session.getToken().getPreferredUsername();
			this.email = session.getToken().getEmail();
			this.customAttributes = session.getToken().getOtherClaims();
			String _costCenter = (String) this.customAttributes.get( "cost-center" );
			String[] costCenters = _costCenter.split( ";" );
			System.out.println(costCenters);

			String tokenAsJson = new ObjectMapper().writeValueAsString(session.getToken());
			logger.info("*** UserInfo Thread ID: {}", Thread.currentThread().getId());
			logger.info("Token in UserInfo: {}", tokenAsJson);


		}
	}

	public String getUsername() {
		return username;
	}

	public String getEmail() {
		return email;
	}

	public Map<String, Object> getCustomAttributes() {
		return customAttributes;
	}

	@Override
	public String toString() {
		return "UserInfo{" +
			"username='" + username + '\'' +
			", email='" + email + '\'' +
			", customAttributes=" + customAttributes +
			'}';
	}
}