package com.example.ImageHandling.controller;

import com.example.ImageHandling.domains.ExtraDocument;
import com.example.ImageHandling.services.ExtraDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.example.ImageHandling.utils.ResponseUtil.createErrorResponse;
import static com.example.ImageHandling.utils.SortUtil.getSortingOrders;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 2/21/2025
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping( "/api/extra-documents" )
public class ExtraDocumentsController {

	private static final Logger logger = LoggerFactory.getLogger( ExtraDocumentsController.class );

	private final ExtraDocumentService extraDocumentService;




	@PostMapping( "/getAll" )
	public ResponseEntity<?> getAllExtraDocuments(
		@RequestBody Map<String, Object> requestBody

	) {
		int page = Integer.parseInt( requestBody.getOrDefault( "page", "0" ).toString() );
		int size = Integer.parseInt( requestBody.getOrDefault( "size", "10" ).toString() );
		String sort = requestBody.getOrDefault( "sort", "createdAt,desc" ).toString();
		String name = requestBody.getOrDefault( "name", "" ).toString();
		String type = requestBody.getOrDefault( "type", "" ).toString();
		String fileName = requestBody.getOrDefault( "fileName", "" ).toString();
		String month = requestBody.getOrDefault( "month", "" ).toString();

		Pageable pageable = PageRequest.of( page, size, Sort.by( getSortingOrders( sort.split( "," ) ) ) );
		List<ExtraDocument> results = extraDocumentService.findAll( pageable, name, type, fileName, month );
		return ResponseEntity.ok( results );
	}

	@GetMapping( "/count" )
	public ResponseEntity<?> getExtraDocumentCount() {
			return ResponseEntity.ok( extraDocumentService.countAll() );
	}

	@PostMapping( "/upload" )
	public ResponseEntity<?> uploadExtraDocument(
		@RequestParam( "file" ) MultipartFile file,
		@RequestParam( "type" ) String type,
		@RequestParam( "name" ) String name,
		@RequestParam( "month" ) String month,
		@RequestParam( "description" ) String description
	) throws IOException {
			ExtraDocument extraDocument = new ExtraDocument( type, name, file.getOriginalFilename(), month, description, file.getBytes() );
			extraDocumentService.saveExtraDocument( extraDocument );
			return ResponseEntity.ok( "The file has been uploaded successfully" );

	}

	@PreAuthorize("hasAnyAuthority(@deleteInvoicesAllowedRoles)")
	@DeleteMapping( "/{id}" )
	public ResponseEntity<?> deleteExtraDocument( @PathVariable( "id" ) String id ) {
		extraDocumentService.deleteExtraDocumentById( id );
			return ResponseEntity.noContent( ).build();

	}

	@GetMapping("/download/{id}")
	public ResponseEntity<?> downloadExtraDocumentFile( @PathVariable( "id" ) String id){
			ExtraDocument fetchedDocument = extraDocumentService.findExtraDocumentById( id );
			if ( fetchedDocument == null ) {
				return ResponseEntity.notFound().build();
			}
			byte[] downloadedFile = extraDocumentService.downloadExtraDocumentFile( id );
			return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fetchedDocument.getFileName() + "\"")
				.body(downloadedFile);

	}

	@PostMapping("/download")
	public ResponseEntity<?> downloadExtraDocumentFiles( @RequestBody List<String> idsForDownload ){
		byte[] zipBytes = extraDocumentService.downloadExtraDocumentFiles( idsForDownload );
		if ( zipBytes == null ) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(zipBytes);
	}
}
