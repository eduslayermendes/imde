package com.example.ImageHandling.services;

import com.example.ImageHandling.domains.dto.UserDetailsDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 10/11/2024
 */

@Slf4j
@Service
public class AuthService {

	private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
	public static final String UNKNOWN_USER = "UNKNOWN_USER";

	public UserDetailsDTO getLoggedInUserDetails() {
		try {
			KeycloakAuthenticationToken authentication =
				(KeycloakAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

			if (authentication != null) {
				KeycloakPrincipal<KeycloakSecurityContext> principal =
					(KeycloakPrincipal<KeycloakSecurityContext>) authentication.getPrincipal();

				KeycloakSecurityContext session = principal.getKeycloakSecurityContext();

				return mapToUserDetailsDTO( session );
			} else {
				logger.warn("Authentication is null or user is not authenticated.");
				return null;
			}
		} catch (Exception e) {
			String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
			logger.error("Error while fetching logged-in user details. Error message: {}", rootCauseMessage, e);
			return null;
		}
	}

	private static UserDetailsDTO mapToUserDetailsDTO( KeycloakSecurityContext session ) {
		UserDetailsDTO userDetails = new UserDetailsDTO();
		String preferredUsername = session.getToken().getPreferredUsername();
		userDetails.setUsername(preferredUsername != null && !preferredUsername.isEmpty() ? preferredUsername :
			session.getToken().getName() != null ? session.getToken().getName() :
			session.getToken().getEmail() != null ? session.getToken().getEmail() :
			session.getToken().getSubject() != null ? session.getToken().getSubject() : UNKNOWN_USER);
		String givenName = session.getToken().getGivenName();
		String familyName = session.getToken().getFamilyName();
		userDetails.setFullName((givenName != null && !givenName.isEmpty() && familyName != null && !familyName.isEmpty())
			? givenName + " " + familyName : UNKNOWN_USER);

		String email = session.getToken().getEmail();
		userDetails.setEmail(email != null && !email.isEmpty() ? email : UNKNOWN_USER);

		String userId = session.getToken().getSubject();
		userDetails.setUserId(userId != null && !userId.isEmpty() ? userId : UNKNOWN_USER);

		userDetails.setRoles(new ArrayList<>( session.getToken().getRealmAccess().getRoles() != null
			&& !session.getToken().getRealmAccess().getRoles().isEmpty()
			? session.getToken().getRealmAccess().getRoles()
			: Collections.emptyList()));

		Map<String, Object> customClaims = session.getToken().getOtherClaims();
		userDetails.setCustomClaims(customClaims != null ? customClaims : new HashMap<>());

		if (customClaims != null && !customClaims.isEmpty()) {
			String costCenters = (String) customClaims.get("cost-center");
			if (costCenters != null) {
				userDetails.setCostCenters(costCenters.split(";"));
			}
		}

		if (session.getToken().getResourceAccess() != null) {
			Map<String, Object> resourceAccess = new HashMap<>();
			session.getToken().getResourceAccess().forEach((key, value) -> resourceAccess.put(key, new ObjectMapper().convertValue(value, Map.class)));
			if (resourceAccess != null && resourceAccess.containsKey("backend")) {
				Map<String, Object> clientRoles = (Map<String, Object>) resourceAccess.get("backend");
				if (clientRoles.containsKey("roles")) {
					userDetails.setPermissions((List<String>) clientRoles.get("roles"));
				}
			} else {
				userDetails.setPermissions(Collections.emptyList());
			}
		}


		return userDetails;
	}

	public boolean hasPermission( String requiredPermission) {
		UserDetailsDTO userDetails = getLoggedInUserDetails();
		return userDetails != null && userDetails.getPermissions().contains(requiredPermission);
	}
}

