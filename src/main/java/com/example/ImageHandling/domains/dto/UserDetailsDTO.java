package com.example.ImageHandling.domains.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 10/11/2024
 */
@Data
public class UserDetailsDTO {
	private String username;
	private String fullName;
	private String email;
	private String userId;
	private List<String> roles;
	private List<String> permissions;
	private Map<String, Object> customClaims;
	private String[] costCenters;


}

