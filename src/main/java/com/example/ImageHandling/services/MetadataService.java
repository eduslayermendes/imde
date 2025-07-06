package com.example.ImageHandling.services;

import com.example.ImageHandling.domains.BatchProcessFile;
import com.example.ImageHandling.domains.InvoiceMetadata;
import com.example.ImageHandling.domains.Invoices;
import com.example.ImageHandling.domains.Item;
import com.example.ImageHandling.domains.repository.InvoicesRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

import static com.example.ImageHandling.domains.types.LogOperation.INVOICE_UPDATED;

@Service
@RequiredArgsConstructor
public class MetadataService {

    private static final Logger logger = LoggerFactory.getLogger(MetadataService.class);
	private final BatchService batchService;
	private final InvoicesRepository invoicesRepository;
	private final AuditLogService auditLogService;
	private final AuthService authService;

	public void editMetadata( String batchProcessFileId, InvoiceMetadata editedMetadata ) {
		logger.info("Attempting to edit metadata for batch process file id: {}", batchProcessFileId);
		Optional<BatchProcessFile> optionalBatchProcessFile = batchService.findBatchProcessFileById( batchProcessFileId );
		if ( optionalBatchProcessFile.isEmpty() ) {
			throw new RuntimeException( "Batch process file not found" );
		}

		BatchProcessFile batchProcessFile = optionalBatchProcessFile.get();
		batchProcessFile.setMetadata( editedMetadata );
		batchProcessFile.setUpdatedAt( LocalDateTime.now() );
		batchProcessFile.setState( "EDITED" ); // Set state to "EDITED"
		batchService.updateBatchProcessFile( batchProcessFile );
		logger.info("Updated batch process file metadata. id: {}", batchProcessFile.getId());
	}

	public Map<String, Object> getBatchProcessFileById( String batchProcessFileId ) {
		logger.info("Attempting to get batch process file by id: {}", batchProcessFileId);
		Optional<BatchProcessFile> optionalBatchProcessFile = batchService.findBatchProcessFileById(batchProcessFileId);
		if (optionalBatchProcessFile.isEmpty()) {
			logger.error( "Batch process file not found. id: {}", batchProcessFileId );
			throw new RuntimeException( "Batch process file not found" );
		}

		BatchProcessFile batchProcessFile = optionalBatchProcessFile.get();
		Map<String, Object> fileDetails = new HashMap<>();
		fileDetails.put("filename", batchProcessFile.getFilename());
		fileDetails.put("fileId", batchProcessFile.getId());
		fileDetails.put("layout", batchProcessFile.getLayout());

		InvoiceMetadata metadata = batchProcessFile.getMetadata();
		if (metadata != null) {
			fileDetails.put("metadata", getStringObjectMap(metadata));
		}
		return fileDetails;
	}

	private static Map<String, Object> getStringObjectMap(InvoiceMetadata metadata) {
		Map<String, Object> metadataMap = new HashMap<>();
		metadataMap.put("invoiceNumber", metadata.getInvoiceNumber());
		metadataMap.put("invoiceDate", metadata.getInvoiceDate());
		metadataMap.put("total", metadata.getTotal());
		metadataMap.put("companyName", metadata.getCompanyName());
		metadataMap.put("issuerVATNumber", metadata.getIssuerVATNumber());
		metadataMap.put("acquirerVATNumber", metadata.getAcquirerVATNumber());
		metadataMap.put("acquirerCountry", metadata.getAcquirerCountry());
		metadataMap.put("atcud", metadata.getAtcud());
		metadataMap.put("valueAddedTax", metadata.getValueAddedTax());
		metadataMap.put("subtotal", metadata.getSubtotal());

		// Include items
		List<Map<String, Object>> items = new ArrayList<>();
		for ( Item item : metadata.getItems()) {
			Map<String, Object> itemMap = new HashMap<>();
			itemMap.put("itemName", item.getItemName());
			itemMap.put("itemQuantity", item.getItemQuantity());
			itemMap.put("itemValue", item.getItemValue());
			itemMap.put("itemSubtotal", item.getItemSubtotal());
			itemMap.put("totalAmount", item.getTotalAmount());
			itemMap.put("articleRef", item.getArticleRef());
			items.add(itemMap);
		}
		metadataMap.put("items", items);

		return metadataMap;
	}

	public Optional<Invoices> editInvoiceMetadata( String id, InvoiceMetadata updatedMetadata ) {
		logger.info("Attempting to edit invoice metadata. id: {}", id);
		Optional<Invoices> invoiceOptional = invoicesRepository.findById( id );
		if ( invoiceOptional.isPresent() ) {
			Invoices invoice = invoiceOptional.get();
			InvoiceMetadata oldMetadata = invoice.getInvoiceMetadata();

			// Update metadata
			invoice.setInvoiceMetadata( updatedMetadata );
			invoice.setUpdatedAt( LocalDateTime.now() );
			Invoices updatedInvoice = invoicesRepository.save( invoice );

			// Log changes
            logger.info("Updated invoice metadata. id: {}", updatedInvoice.getInvoiceid());
            auditLogService.logInvoiceMetadataChanges( INVOICE_UPDATED.toString(), authService.getLoggedInUserDetails().getUsername(), oldMetadata, updatedMetadata );
			return Optional.of( updatedInvoice );
		}else{
			return Optional.empty();
		}
	}
}
