package com.example.ImageHandling.controller;

import com.example.ImageHandling.domains.ExtraDocumentType;
import com.example.ImageHandling.services.ExtraDocumentTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 3/12/2025
 */
@RestController
@RequestMapping("/api/extra-document-type")
@Slf4j
@RequiredArgsConstructor
public class ExtraDocumentTypeController {

	private final ExtraDocumentTypeService extraDocumentTypeService;


	@PostMapping("/create")
	@PreAuthorize("hasAnyAuthority(@costCentersAllowedRoles)")
	public ResponseEntity<?> addDocumentType( @Valid @RequestBody ExtraDocumentType extraDocumentType){
			ExtraDocumentType createdDocumentType = extraDocumentTypeService.saveOrUpdate( extraDocumentType );
			return ResponseEntity.ok(createdDocumentType);
	}

	@GetMapping
	public ResponseEntity<?> getAllExtraDocumentTypes(){
		List<ExtraDocumentType> allExtraDocTypes = extraDocumentTypeService.findAll();
		return ResponseEntity.ok(allExtraDocTypes);
	}

	@DeleteMapping( "/{id}" )
	@PreAuthorize("hasAnyAuthority(@costCentersAllowedRoles)")
	public ResponseEntity<?> deleteExtraDocumentType(@PathVariable("id") String id){
		 extraDocumentTypeService.deleteDocumentType( id );
		return ResponseEntity.noContent( ).build();
	}

	@PutMapping
	@PreAuthorize("hasAnyAuthority(@costCentersAllowedRoles)")
	public ResponseEntity<?> updateDocumentType(@Valid @RequestBody ExtraDocumentType extraDocumentType ){
		if ( extraDocumentType.getId() == null || extraDocumentTypeService.findById(extraDocumentType.getId() ).isEmpty() ){
			return ResponseEntity.notFound().build();
		} else {
			return ResponseEntity.ok( extraDocumentTypeService.saveOrUpdate( extraDocumentType ) );
		}
	}

}
