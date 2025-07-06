package com.example.ImageHandling.services;

import com.example.ImageHandling.domains.*;
import com.example.ImageHandling.domains.dto.ExtractedDataDTO;
import com.example.ImageHandling.domains.dto.UploadInvoicesDTO;
import com.example.ImageHandling.domains.dto.UserDetailsDTO;
import com.example.ImageHandling.domains.repository.RegexPatternRepository;
import com.example.ImageHandling.domains.types.BatchProcessFileState;
import com.example.ImageHandling.domains.types.BatchProcessState;
import com.example.ImageHandling.exception.DuplicateInvoiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 4/9/2025
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceUploadService {

	private static final Logger logger = LoggerFactory.getLogger( InvoiceUploadService.class );

	private final RegexPatternService regexPatternService;

	private final QRCodeReader qrCodeReader;

	private final ImageService imageService;

	private final ExtractionService extractionService;

	private final BatchService batchService;

	private final RegexPatternRepository regexPatternRepository;

	private final AuditLogService auditLogService;

	private final AuthService authService;

	private final InvoiceService invoiceService;

	public static final int TIMEOUT = 70;

//	@Transactional( timeout = TIMEOUT, rollbackFor = Exception.class )
//	public Optional<UploadInvoicesDTO> uploadInvoice(UploadInvoicesDTO uploadInvoicesDTO) {
//
//	}


	@Transactional( timeout = TIMEOUT, rollbackFor = Exception.class )
	public List<UploadInvoicesDTO> uploadAndExtractInvoice( MultipartFile file, String patternName, String uploadComment, String costCenter, List<String> errors ) throws Exception {
		return doUploadAndExtract( file, patternName, uploadComment, costCenter, errors );
	}

//	public Optional<UploadInvoicesDTO> doUpload(UploadInvoicesDTO uploadInvoicesDTO) {
//
//	}

	public List<UploadInvoicesDTO> doUploadAndExtract(MultipartFile file,
	            String patternName,
	            String uploadComment,
	            String costCenter,
	            List<String> errors) throws Exception {
	    logger.debug("Start uploading and extracting invoice data");
	    List<UploadInvoicesDTO> results = new ArrayList<>();

	    if (file == null || file.getOriginalFilename() == null) {
	        throw new IllegalArgumentException("File is null");
	    }

	    logger.info("Processing file {} with pattern {}", Objects.requireNonNull(file).getOriginalFilename(), patternName);
	    String patternId = regexPatternService.getPatternIdByName(patternName);
	    String fileType = getFileType(file);

	    BatchProcessFile batchProcessFile = new BatchProcessFile();
	    batchProcessFile.setFilename(file.getOriginalFilename());
	    batchProcessFile.setFiletype(fileType);
	    batchProcessFile.setContent(file.getBytes());
	    batchProcessFile.setLayout(patternName);
	    batchProcessFile.setState(BatchProcessFileState.UPLOADED.name());
	    batchProcessFile.setComment(uploadComment);
	    batchProcessFile.setCostCenter(costCenter);

	    if ("PT".equals(patternName)) {
	        List<ExtractedDataDTO> extractedDataDTOs;
	        if (isPDFFile(fileType)) {
	            extractedDataDTOs = qrCodeReader.processPDF(file, batchProcessFile);
	        } else if (isValidImageFileType(fileType)) {
	            extractedDataDTOs = qrCodeReader.processImage(file, batchProcessFile);
	        } else {
	            throw new IllegalArgumentException("Invalid file type");
	        }

	        // Process each extracted invoice
	        for (ExtractedDataDTO dto : extractedDataDTOs) {
	            BatchProcess batchProcess = batchService.createBatchProcess();

	            // Create a new BatchProcessFile for each extracted invoice
	            BatchProcessFile individualBatchFile = new BatchProcessFile();
	            individualBatchFile.setFilename(file.getOriginalFilename());
	            individualBatchFile.setFiletype(fileType);
	            individualBatchFile.setContent(file.getBytes());
	            individualBatchFile.setLayout(patternName);
	            individualBatchFile.setState(BatchProcessFileState.REVIEW.name());
	            individualBatchFile.setComment(uploadComment);
	            individualBatchFile.setCostCenter(costCenter);
	            individualBatchFile.setMetadata(dto.getInvoiceMetadata());
	            individualBatchFile.setProcessId(batchProcess.getId());

	            // Save batch process file
	            batchService.createBatchProcessFile(individualBatchFile);

	            // Update batch process state
	            batchProcess.setState(BatchProcessState.REVIEW.name());
	            batchService.updateBatchProcess(batchProcess);

	            // Add to results
	            UploadInvoicesDTO uploadDto = new UploadInvoicesDTO();
	            uploadDto.setBatchProcess(batchProcess);
	            results.add(uploadDto);
	        }
	    } else {
	        // Handle non-PT files (existing code)
	        ExtractedDataDTO extractedDataDTO;
	        if (isPDFFile(fileType)) {
	            extractedDataDTO = extractionService.extractTextFromPDFWithPattern(file, patternId);
	            extractedDataDTO.getInvoiceMetadata().setCostCenter(costCenter);
	            batchProcessFile.setMetadata(extractedDataDTO.getInvoiceMetadata());
	        } else if (isValidImageFileType(fileType)) {
	            extractedDataDTO = extractImageText(file, patternId, batchProcessFile);
	            extractedDataDTO.getInvoiceMetadata().setCostCenter(costCenter);
	        } else {
	            throw new IllegalArgumentException("Invalid file type");
	        }

	        if (extractedDataDTO != null && Boolean.TRUE.equals(extractedDataDTO.getIsExtractedData())) {
	            processExtractedData(extractedDataDTO, batchProcessFile);
				BatchProcess batchProcess = batchService.findBatchProcessById(batchProcessFile.getProcessId())
						.orElseThrow(() -> new IllegalStateException("Batch process not found for ID: " + batchProcessFile.getProcessId()));
				UploadInvoicesDTO uploadDto = new UploadInvoicesDTO();
	            uploadDto.setBatchProcess(batchProcess);
	            results.add(uploadDto);
	        }
	    }

	    auditLogService.logUpload(getLoggedInUsername(), file.getOriginalFilename());
	    return results;
	}

	private void processExtractedData(ExtractedDataDTO dto, BatchProcessFile batchProcessFile) {
		// Remove redundant nested check and use Boolean.TRUE.equals()
		if (dto != null && Boolean.TRUE.equals(dto.getIsExtractedData())) {
			logger.info("Extracted invoice metadata: {}", dto.getInvoiceMetadata());

			if (isInvoiceDuplicated(dto)) {
				logger.error("Invoice {} is duplicated", dto.getInvoiceMetadata().getInvoiceNumber());
				throw new DuplicateInvoiceException("Duplicate invoice detected: " +
						dto.getInvoiceMetadata().getInvoiceNumber());
			}

			// Create the batch process
			BatchProcess batchProcess = batchService.createBatchProcess();

			// Update the batch process file
			batchProcessFile.setProcessId(batchProcess.getId());
			batchProcessFile.setState(BatchProcessFileState.REVIEW.name());
			batchService.createBatchProcessFile(batchProcessFile);

			// Update batch process state
			batchProcess.setState(BatchProcessState.REVIEW.name());
			batchService.updateBatchProcess(batchProcess);
		}
	}

	private String getLoggedInUsername() {
		UserDetailsDTO loggedInUserDetails = authService.getLoggedInUserDetails();
		if ( loggedInUserDetails != null ) {
			var userDetails = authService.getLoggedInUserDetails();
			if ( userDetails.getUsername() != null ) {
				return userDetails.getUsername();
			}
			if ( userDetails.getFullName() != null ) {
				return userDetails.getFullName();
			}
			if ( userDetails.getEmail() != null ) {
				return userDetails.getEmail();
			}
		}
		return AuthService.UNKNOWN_USER;
	}

	private static boolean isPDFFile( String fileType ) {
		return "pdf".equalsIgnoreCase( fileType );
	}

	private Boolean isInvoiceDuplicated( ExtractedDataDTO extractedDataDTO ) {
		Optional<List<Invoices>> duplicateInvoices = invoiceService.findDuplicateInvoices( extractedDataDTO.getInvoiceMetadata().getIssuerVATNumber(),
			extractedDataDTO.getInvoiceMetadata().getInvoiceDate(),
			extractedDataDTO.getInvoiceMetadata().getInvoiceNumber() );
		return duplicateInvoices.isPresent() && !duplicateInvoices.get().isEmpty();
	}

	private boolean isValidImageFileType( String fileType ) {
		return "jpg".equalsIgnoreCase( fileType ) || "jpeg".equalsIgnoreCase( fileType ) || "png".equalsIgnoreCase( fileType );
	}

	private ExtractedDataDTO extractImageText( MultipartFile file, String patternId, BatchProcessFile batchProcessFile ) throws Exception {
		ExtractedDataDTO extractedDataDTO = new ExtractedDataDTO();
		// Decode the image to the Mat
		Mat image = Imgcodecs.imdecode( new MatOfByte( file.getBytes() ), Imgcodecs.IMREAD_COLOR );
		// Retrieve the regex pattern by ID and determine the language
		RegexPattern regexId = regexPatternRepository.findById( patternId ).orElseThrow();
		String language = regexId.getLanguage();
		// Extract text from the image using OCR
		String extractedText = imageService.extractText( image, language );
		// Extract metadata from the extracted text based on the regex pattern
		InvoiceMetadata invoiceMetadata = extractionService.extractAndLabelText( extractedText, patternId );
		invoiceMetadata.setOriginalFileName( file.getOriginalFilename() );
		batchProcessFile.setMetadata( invoiceMetadata );
		extractedDataDTO.setIsExtractedData( true );
		extractedDataDTO.setInvoiceMetadata( invoiceMetadata );
		return extractedDataDTO;
	}

	private String getFileType( MultipartFile file ) {
		String fileName = file.getOriginalFilename();
		if ( StringUtils.hasText( fileName ) && fileName.contains( "." ) ) {
			fileName = fileName.toLowerCase();
			return fileName.substring( fileName.lastIndexOf( "." ) + 1 );
		}
		else {
			throw new IllegalArgumentException( "File name must contain a file extension" );
		}
	}
}

