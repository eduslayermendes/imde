package com.example.ImageHandling.controller;

import com.example.ImageHandling.domains.BatchProcessFile;
import com.example.ImageHandling.domains.Invoices;
import com.example.ImageHandling.domains.dto.ErrorResponse;
import com.example.ImageHandling.domains.dto.SaveInvoiceResponseDto;
import com.example.ImageHandling.services.AuthService;
import com.example.ImageHandling.services.InvoiceService;
import com.example.ImageHandling.services.TableService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.example.ImageHandling.utils.SortUtil.getSortingOrders;
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping( "/api/invoices" )
public class InvoiceController {

	private static final Logger logger = LoggerFactory.getLogger(InvoiceController.class);

	private final InvoiceService invoiceService;

	private final TableService tableService;

	private final AuthService authService;


	@GetMapping( "/recent" )
	public ResponseEntity<?> getRecentProcessedFiles() {
			List<Invoices> recentFiles = invoiceService.findTop20ByOrderByCreatedAtDesc();
			return ResponseEntity.ok( recentFiles );
	}

	@PostMapping( "/batch/save" )
	public ResponseEntity<?> saveExtractedData( @RequestBody List<String> batchProcessFileIds ) {
		SaveInvoiceResponseDto processedData;

		processedData = invoiceService.saveExtractedData( batchProcessFileIds );
		// Always return 200 OK with the response body, even if only duplicates
		return ResponseEntity.ok(processedData);

	}

	@GetMapping( "/by-ids" )
	public ResponseEntity<?> getInvoicesByIds( @RequestParam List<String> ids ) {
			List<Invoices> invoices = invoiceService.findInvoicesByIds( ids );
			if ( invoices.isEmpty() ) {
				return ResponseEntity.status( HttpStatus.NO_CONTENT ).body( invoices );
			}
			return ResponseEntity.status( HttpStatus.OK ).body( invoices );

	}

	/**
	 * Endpoint to retrieve invoice data.
	 *
	 * @return List of String arrays containing invoice data
	 */
	@GetMapping
	@PreAuthorize("hasAnyAuthority(@readInvoicesAllowedRoles) and @authService.hasPermission('INVOICES_READ')")
	public ResponseEntity<?> getInvoiceData(
		@RequestParam(defaultValue = "0") Integer pageNo,
		@RequestParam(defaultValue = "10") Integer pageSize,
		@RequestParam(defaultValue = "createdAt,desc") String[] sort,
		@RequestParam(required = false) String searchFileName,
		@RequestParam(required = false) String issuer,
		@RequestParam(required = false) String createdBy,
		@RequestParam(required = false) String createdAtStartDate,
		@RequestParam(required = false) String createdAtEndDate,
		@RequestParam(required = false) String invoiceStartDate,
		@RequestParam(required = false) String invoiceEndDate,
		@RequestParam(required = false) String invoiceNumber,
		@RequestParam(required = false) String costCenter
	) {
			List<Invoices> invoiceData = tableService.getAllInvoicesWithPaginationFilters(
				pageNo, pageSize, Sort.by(getSortingOrders(sort)), searchFileName, issuer, createdBy, createdAtStartDate, createdAtEndDate, invoiceStartDate, invoiceEndDate, invoiceNumber, costCenter, authService.getLoggedInUserDetails().getCostCenters());
			if (!invoiceData.isEmpty()) {
				return ResponseEntity.ok(invoiceData);
			} else {
				return ResponseEntity.status(HttpStatus.NO_CONTENT).body(invoiceData);
			}
	}

	@GetMapping( "/count" )
	public ResponseEntity<?> getInvoiceCount(
		@RequestParam( required = false ) String searchFileName,
		@RequestParam( required = false ) String issuer,
		@RequestParam( required = false ) String createdBy,
		@RequestParam(required = false) String createdAtStartDate,
		@RequestParam(required = false) String createdAtEndDate,
		@RequestParam( required = false ) String invoiceStartDate,
		@RequestParam( required = false ) String invoiceEndDate,
		@RequestParam(required = false) String invoiceNumber,
		@RequestParam(required = false) String costCenter
	) {
			long count = invoiceService.getFilteredInvoiceCount( searchFileName, issuer, createdBy, createdAtStartDate, createdAtEndDate, invoiceStartDate, invoiceEndDate, invoiceNumber, costCenter, authService.getLoggedInUserDetails().getCostCenters() );
			return ResponseEntity.ok().body( count );

	}

	/**
	 * Endpoint to delete an invoice by ID.
	 *
	 * @param id ID of the invoice to delete
	 */
	@PreAuthorize("hasAnyAuthority(@deleteInvoicesAllowedRoles)")
	@DeleteMapping( "/{id}" )
	public ResponseEntity<?> deleteInvoice( @PathVariable String id ) {
			tableService.deleteInvoice( id );
			return ResponseEntity.noContent().build();

	}




	@GetMapping( "/duplicate" )
	public ResponseEntity<?> findDuplicateInvoices(
		@RequestParam() String issuerVATNumber,
		@RequestParam @DateTimeFormat( iso = DateTimeFormat.ISO.DATE ) LocalDate invoiceDate,
		@RequestParam() String invoiceNumber
	) {

			Optional<List<Invoices>> duplicateInvoices = invoiceService.findDuplicateInvoices( issuerVATNumber, invoiceDate, invoiceNumber );

			if ( duplicateInvoices.isPresent() && !duplicateInvoices.get().isEmpty() ) {
				return ResponseEntity.ok( duplicateInvoices.get() );
			}
			else {
				return ResponseEntity.noContent().build();
			}


	}


}
