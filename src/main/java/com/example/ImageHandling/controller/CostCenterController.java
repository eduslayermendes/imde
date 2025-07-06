package com.example.ImageHandling.controller;

import com.example.ImageHandling.domains.CostCenter;
import com.example.ImageHandling.services.AuthService;
import com.example.ImageHandling.services.CostCenterService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 1/31/2025
 */

@RestController
@RequestMapping( "/api/cost-centers" )
@Slf4j
@AllArgsConstructor
public class CostCenterController {

	private final CostCenterService costCenterService;

	/**
	 * Fetch all CostCenters.
	 *
	 * @return List of all CostCenters.
	 */
	@GetMapping
	public ResponseEntity<?> getAllCostCenters() {
		return ResponseEntity.ok( costCenterService.findAll() );
	}

	@GetMapping( "/actives" )
	public ResponseEntity<?> getAllActiveCostCenters() {
		List<CostCenter> activeCostCenters = costCenterService.findActiveCostCenters();
		if ( !activeCostCenters.isEmpty() ) {
			return new ResponseEntity<>( activeCostCenters, HttpStatus.OK );
		}
		else {
			return new ResponseEntity<>( HttpStatus.NO_CONTENT );
		}

	}

	@GetMapping( "/user/actives" )
	public ResponseEntity<?> getUserActiveCostCenters() {
		List<CostCenter> activeCostCenters = costCenterService.findUserActiveCostCenters();
		if ( !activeCostCenters.isEmpty() ) {
			return new ResponseEntity<>( activeCostCenters, HttpStatus.OK );
		}
		else {
			return new ResponseEntity<>( HttpStatus.NO_CONTENT );
		}

	}

	/**
	 * Fetch a specific CostCenter by ID.
	 *
	 * @param id the ID of the CostCenter.
	 * @return the CostCenter if found, or 404 if not.
	 */
	@GetMapping( "/{id}" )
	public ResponseEntity<?> getCostCenterById( @PathVariable String id ) {
		return costCenterService.findById( id )
			.map( ResponseEntity::ok )
			.orElse( ResponseEntity.notFound().build() );
	}

	/**
	 * Create a new CostCenter.
	 *
	 * @param costCenter the CostCenter to create.
	 * @return the created CostCenter.
	 */
	@PostMapping( "/create" )
	@PreAuthorize( "hasAnyAuthority(@costCentersAllowedRoles)" )
	public ResponseEntity<?> createCostCenter( @Valid @RequestBody CostCenter costCenter ) {
		return ResponseEntity.ok( costCenterService.saveOrUpdate( costCenter ) );
	}

	/**
	 * Update an existing CostCenter.
	 *
	 * @param costCenter the updated CostCenter.
	 * @return the updated CostCenter if it exists, or 404 if not.
	 */
	@PutMapping()
	@PreAuthorize( "hasAnyAuthority(@costCentersAllowedRoles)" )
	public ResponseEntity<?> updateCostCenter( @Valid @RequestBody CostCenter costCenter ) {

		if ( costCenter.getId() == null || costCenterService.findById( costCenter.getId() ).isEmpty() ) {
			return ResponseEntity.notFound().build();
		}
		else {
			return ResponseEntity.ok( costCenterService.saveOrUpdate( costCenter ) );
		}

	}

}

