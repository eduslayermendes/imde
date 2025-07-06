package com.example.ImageHandling.services;

import com.example.ImageHandling.domains.ExtraDocument;
import com.example.ImageHandling.domains.repository.AuditLogRepository;
import com.example.ImageHandling.domains.repository.ExtraDocumentsRepository;
import com.example.ImageHandling.domains.types.LogOperation;
import com.example.ImageHandling.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 2/21/2025
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExtraDocumentService {

	private final ExtraDocumentsRepository extraDocumentsRepository;
	private final MongoTemplate mongoTemplate;
	private final AuthService authService;
	private final AuditLogRepository auditLogRepository;

	private static final Logger logger = LoggerFactory.getLogger(ExtraDocumentService.class);

	private final AuditLogService auditLogService;

	public ExtraDocument findExtraDocumentById( String id ) {
		logger.info( "Attempting to find extra document with id: {}", id );
		return extraDocumentsRepository.findById( id ).orElseThrow( () ->
			new ResourceNotFoundException( "Extra document not found for id: " + id ) );
	}

	public ExtraDocument saveExtraDocument(ExtraDocument extraDocument) {
		logger.info("Attempting to save extra document: {}", extraDocument.getFileName());

		ExtraDocument savedDocument = extraDocumentsRepository.save(extraDocument);
		if ( savedDocument != null) {
			auditLogService.persistAuditLog(savedDocument, LogOperation.EXTRA_DOCUMENT_UPLOADED );
			logger.info("Extra document saved successfully: {}", savedDocument);
		} else {
			logger.warn("Failed to save the extra document: {}", extraDocument);
		}
		return savedDocument;
	}


	public List<ExtraDocument> findAll(Pageable pageable,String name, String type, String fileName, String month){
		Query query = new Query().with( pageable );
		if (fileName != null && !fileName.isEmpty()) {
			query.addCriteria( Criteria.where( "fileName" ).regex( fileName, "i") );
		}
		if (type != null && !type.isEmpty()) {
			query.addCriteria( Criteria.where( "type" ).regex( type, "i") );
		}
		if (name != null && !name.isEmpty()) {
			query.addCriteria( Criteria.where( "name" ).regex( name, "i") );
		}
		if (month != null && !month.isEmpty()) {
			query.addCriteria( Criteria.where( "month" ).regex( month, "i") );
		}

		List<ExtraDocument> extraDocuments = mongoTemplate.find( query, ExtraDocument.class );
		return extraDocuments;

	}


	public Long countAll() {
		logger.info("Counting all extra documents");
		return extraDocumentsRepository.count();
	}

	public void deleteExtraDocumentById( String id ) {
		logger.info("Attempting to delete extra document with id: {}", id);
		ExtraDocument fetchedDocument = this.findExtraDocumentById( id );
		if ( fetchedDocument == null ) {
			throw new ResourceNotFoundException("ExtraDocument with id " + id + " not found");
		}
		extraDocumentsRepository.deleteById( id );
		auditLogService.persistAuditLog( fetchedDocument, LogOperation.EXTRA_DOCUMENT_DELETED );
	}
	
	public byte[] downloadExtraDocumentFile( String id ) {
		logger.info("Attempting to download extra document file with id: {}", id);
		if ( id == null || id.isEmpty() ) {
			logger.error( "Error on downloading the extra document file, Id is empty"  );
			throw new IllegalArgumentException( "The id of the extra document cannot be null" );
		}
		ExtraDocument extraDocument = extraDocumentsRepository.findById( id ).orElse( null );
		if ( extraDocument == null ) {
			return null;
		}

		auditLogService.persistAuditLog( extraDocument, LogOperation.EXTRA_DOCUMENT_EXPORTED );
		return extraDocument.getFile();
	}

	public byte[] downloadExtraDocumentFiles(List<String> idsForDownload) {
		if (idsForDownload == null || idsForDownload.isEmpty()) {
			logger.error("Error downloading extra documents: ID list is empty");
			throw new IllegalArgumentException("The list of IDs for extra document download is empty.");
		}
		logger.info("Attempting to download extra documents with IDs: {}", idsForDownload);

		Iterable<ExtraDocument> documentsIterable = extraDocumentsRepository.findAllById(idsForDownload);
		List<ExtraDocument> documentsForDownload = new ArrayList<>();
		documentsIterable.forEach(documentsForDownload::add);

		if (documentsForDownload.isEmpty()) {
			logger.error("No files found for the provided IDs: {}", idsForDownload);
			return null;
		}

		String zipFileName = String.format("ExtraDocuments_%s.zip",
			LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")));

		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {


			for (ExtraDocument document : documentsForDownload) {
				if (document.getFile() == null || document.getFile().length == 0) {
					logger.warn("Skipping document with ID {} as it contains no file data", document.getId());
					continue;
				}

				String typeFolder = document.getType();
				String monthFolder = document.getMonth();

				String basePath = typeFolder + "/" + monthFolder;
				String fileName = document.getFileName();

				if (documentsForDownload.stream().filter(d -> Objects.equals(d.getFileName(), fileName)
					&& Objects.equals(d.getMonth(), document.getMonth())).count() > 1) {

					String createdAtFolder = document.getCreatedAt().toString();
					basePath += "/" + createdAtFolder;
				}

				// Add entry to the ZIP
				String fileEntryPath = basePath + "/" + fileName;
				ZipEntry zipEntry = new ZipEntry(fileEntryPath);
				zipOutputStream.putNextEntry(zipEntry);
				zipOutputStream.write(document.getFile());
				zipOutputStream.closeEntry();

				auditLogService.persistAuditLog(document, LogOperation.EXTRA_DOCUMENT_EXPORTED);
			}

			zipOutputStream.finish();
			logger.info("Successfully created ZIP file: {}", zipFileName);
			return byteArrayOutputStream.toByteArray();

		} catch (IOException e) {
			String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
			logger.error("Error while creating ZIP file: {}, error: {}", zipFileName, rootCauseMessage, e);
			throw new RuntimeException(String.format("Failed to generate ZIP file: %s", rootCauseMessage), e);
		}
	}




}
