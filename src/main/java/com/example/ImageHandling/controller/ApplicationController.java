package com.example.ImageHandling.controller;

import com.example.ImageHandling.domains.dto.ErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.example.ImageHandling.utils.ResponseUtil.createErrorResponse;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 10/28/2024
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping( "/api/app" )
public class ApplicationController {


	@Value( "${application.version}" )
	private String version;

	@GetMapping( "/version" )
	public ResponseEntity<?> getVersion() {
			return new ResponseEntity<>( version, HttpStatus.OK );
	}

}
