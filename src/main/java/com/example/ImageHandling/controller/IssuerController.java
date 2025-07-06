package com.example.ImageHandling.controller;

import com.example.ImageHandling.domains.Issuer;
import com.example.ImageHandling.domains.RegexPattern;
import com.example.ImageHandling.domains.dto.ErrorResponse;
import com.example.ImageHandling.services.IssuerService;
import com.example.ImageHandling.services.RegexPatternService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static com.example.ImageHandling.utils.ResponseUtil.createErrorResponse;
import static com.example.ImageHandling.utils.SortUtil.getSortingOrders;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping( "/api/issuers" )
@PreAuthorize("hasAnyAuthority(@issuersAllowedRoles)")
public class IssuerController {

    private static final Logger logger = LoggerFactory.getLogger(IssuerController.class);
	private final IssuerService issuerService;

	@PostMapping( "/create" )
	public ResponseEntity<?> createIssuer( @RequestBody Issuer issuer ) {
			return ResponseEntity.ok( issuerService.createIssuer( issuer ) );
	}

	@DeleteMapping( "/delete/{id}" )
	public ResponseEntity<?> deleteIssuer( @PathVariable String id ) {
			issuerService.deleteAndLogIssuer( id );
			return ResponseEntity.noContent().build();
	}

	@GetMapping( "/get" )
	public ResponseEntity<?> getAllIssuers(
		@RequestParam( defaultValue = "0" ) Integer pageNo,
		@RequestParam( defaultValue = "10" ) Integer pageSize,
		@RequestParam( defaultValue = "createdAt,desc" ) String[] sort
	) {
			List<Issuer> issuers = issuerService.getAllIssuersWithCaseInsensitiveSorting( pageNo, pageSize, Sort.by(getSortingOrders(sort)) );
			return ResponseEntity.ok( issuers );
	}

	@GetMapping( "/get/{id}" )
	public ResponseEntity<?> getIssuerById( @PathVariable String id ) {

			Optional<Issuer> issuer = issuerService.getIssuerById( id );
			return ResponseEntity.ok( issuer );
	}

	@PutMapping( "/update/{id}" )
	public ResponseEntity<?> updateIssuer( @PathVariable String id, @RequestBody Issuer issuer ) {
			Issuer updatedIssuer = issuerService.updateIssuer( id, issuer );
			return ResponseEntity.ok( updatedIssuer );
	}

	/**
	 * Endpoint to retrieve the count of issuers.
	 *
	 * @return the count of issuers
	 */
	@GetMapping( "/count" )
	public ResponseEntity<?> getIssuerCount() {
			long count = issuerService.getIssuerCount();
			return ResponseEntity.ok().body( count );
	}

}
