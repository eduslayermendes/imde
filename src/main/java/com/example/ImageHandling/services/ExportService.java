package com.example.ImageHandling.services;

import com.example.ImageHandling.domains.dto.IdListRequest;
import com.example.ImageHandling.domains.AuditLog;
import com.example.ImageHandling.domains.InvoiceMetadata;
import com.example.ImageHandling.domains.Invoices;
import com.example.ImageHandling.domains.types.LogOperation;
import com.example.ImageHandling.exception.IllegalDataException;
import com.example.ImageHandling.domains.repository.AuditLogRepository;
import com.example.ImageHandling.domains.repository.InvoicesRepository;
import com.example.ImageHandling.utils.ExcelUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExportService {

	private static final Logger logger = LoggerFactory.getLogger(ExportService.class);

	private final InvoicesRepository invoicesRepository;
	private final AuditLogRepository downloadLogRepository;
	private final AuthService authService;

	public byte[] downloadSelectedFiles( IdListRequest idListRequest, String language ) {
		logger.info("Attempting to download selected files from the list request {}", idListRequest);
		if ( isNull( idListRequest ) ) {
			logger.error( "Error on downloading the file, List Request Id is empty"  );
			throw new IllegalDataException( "List Request Id is empty" );
		}
		try {
			List<String> ids = idListRequest.getIds();
			List<Invoices> invoicesList = ids.stream()
				.map( id -> invoicesRepository.findById( id ).orElse( null ) )
				.collect( Collectors.toList() );

			LocalDateTime firstCreatedAt = findFirstCreatedAt( invoicesList );
			LocalDateTime lastCreatedAt = findLastCreatedAt( invoicesList );

			String zipFileName = createExportFileName( firstCreatedAt, lastCreatedAt );

			byte[] zipBytes = ExcelUtils.generateExcelFile( invoicesList, language, zipFileName );

			List<String> fileNames = invoicesList.stream()
				.filter( invoice -> invoice != null )
				.map( Invoices::getFileName )
				.collect( Collectors.toList() );

			List<InvoiceMetadata> metadataList = invoicesList.stream()
				.filter( invoice -> invoice != null )
				.map( Invoices::getInvoiceMetadata )
				.collect( Collectors.toList() );

			AuditLog log = new AuditLog();
			log.setUsername( authService.getLoggedInUserDetails().getUsername() );
			log.setOperation( LogOperation.EXPORT.toString() );
			log.setFileName( zipFileName );
			log.setTimestamp( LocalDateTime.now() );
			log.setExportedFileNames( fileNames );
			log.setExportedMetadata( metadataList );
			downloadLogRepository.save( log );

			logger.info("Downloaded the invoices {}", fileNames);
			return zipBytes;
		}
		catch ( Exception e ) {
			String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
			logger.error( "Error on downloading the invoices. {}", rootCauseMessage, e );
			throw new RuntimeException( String.format( "Error on downloading the invoices. %s", rootCauseMessage ) );
		}
	}

	public static String createExportFileName( LocalDateTime firstCreatedAt, LocalDateTime lastCreatedAt ) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "yyyy-MM-dd" );
		String formattedFirstDate = firstCreatedAt != null ? firstCreatedAt.format( formatter ) : "unknown";
		String formattedLastDate = lastCreatedAt != null ? lastCreatedAt.format( formatter ) : "unknown";
		return String.format( "Invoices_%s_to_%s.zip", formattedFirstDate, formattedLastDate );
	}

	public HttpHeaders createHeaders( long contentLength, List<Invoices> invoicesList ) {
		LocalDateTime firstCreatedAt = findFirstCreatedAt( invoicesList );
		LocalDateTime lastCreatedAt = findLastCreatedAt( invoicesList );

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType( MediaType.APPLICATION_OCTET_STREAM );

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "yyyy-MM-dd" );
		String formattedFirstDate = firstCreatedAt != null ? firstCreatedAt.format( formatter ) : "unknown";
		String formattedLastDate = lastCreatedAt != null ? lastCreatedAt.format( formatter ) : "unknown";
		String filename = String.format( "Invoices_%s_to_%s.zip", formattedFirstDate, formattedLastDate );

		headers.set( HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename );
		headers.setContentLength( contentLength );
		return headers;
	}

	private LocalDateTime findLastCreatedAt( List<Invoices> invoicesList ) {
		return invoicesList.stream()
			.filter( invoice -> invoice != null )
			.map( Invoices::getCreatedAt )
			.max( LocalDateTime::compareTo )
			.orElse( null );
	}

	private LocalDateTime findFirstCreatedAt( List<Invoices> invoicesList ) {
		return invoicesList.stream()
			.filter( invoice -> invoice != null )
			.map( Invoices::getCreatedAt )
			.min( LocalDateTime::compareTo )
			.orElse( null );
	}

}
