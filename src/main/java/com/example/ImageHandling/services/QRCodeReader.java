package com.example.ImageHandling.services;

import com.example.ImageHandling.domains.BatchProcessFile;
import com.example.ImageHandling.domains.CostCenter;
import com.example.ImageHandling.domains.InvoiceMetadata;
import com.example.ImageHandling.domains.SubtotalField;
import com.example.ImageHandling.domains.dto.ExtractedDataDTO;
import com.example.ImageHandling.exception.DecoderApiInternalServerErrorException;
import com.example.ImageHandling.exception.DecoderApiNotFoundContentException;
import com.example.ImageHandling.exception.IllegalDataException;
import com.example.ImageHandling.domains.repository.BatchProcessFilesRepository;
import com.example.ImageHandling.exception.NoQRCodeDetectedException;
import com.example.ImageHandling.utils.DateUtils;
import com.example.ImageHandling.utils.QRCodeRepairUtil;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.GenericMultipleBarcodeReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import reactor.core.publisher.Mono;
import java.awt.image.DataBufferByte;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.core.CvType;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * Component class for handling QR Codes.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class QRCodeReader {

	private final BatchProcessFilesRepository batchProcessFileRepository;
	private final BarcodeQRCodeDecoderApiCaller barcodeQRCodeDecoderApiCaller;
	private final ExtractionService extractionService; // Inject ExtractionService

	private static final Logger logger = LoggerFactory.getLogger(QRCodeReader.class);



	private boolean isATInvoiceQRCode(String qrCodeText) {
		if (qrCodeText == null || qrCodeText.isEmpty()) {
			return false;
		}
		// AT QR codes contain specific keys like A (IssuerVATNumber) and F (InvoiceDate)
		String[] pairs = qrCodeText.split("\\*");
		boolean hasVATNumber = false;
		boolean hasInvoiceDate = false;

		for (String pair : pairs) {
			String[] parts = pair.split(":");
			if (parts.length == 2) {
				if (parts[0].equals("A")) hasVATNumber = true;
				if (parts[0].equals("F")) hasInvoiceDate = true;
			}
		}
		return hasVATNumber && hasInvoiceDate;
	}

	/**
	 * Processes a PDF file to detect and handle QR codes.
	 *
	 * @param pdfFile          The PDF file containing QR codes to process.
	 * @param batchProcessFile The BatchProcessFile object to store metadata.
	 * @return true if a QR code is found, false otherwise.
	 * @throws IOException If there's an error reading the PDF file.
	 *
	 *
	 */
	public List<ExtractedDataDTO> processPDF(MultipartFile pdfFile, BatchProcessFile batchProcessFile) {
		if (isNull(batchProcessFile)) {
			logger.error("Error on processing the pdf. Batch process file id is empty");
			throw new IllegalDataException("Batch process file id is empty");
		}

		List<ExtractedDataDTO> extractedDataDTOs = new ArrayList<>();
		List<ExtractedDataDTO> extractedDataList = new ArrayList<>();

		try (PDDocument document = PDDocument.load(pdfFile.getInputStream())) {
			PDFRenderer renderer = new PDFRenderer(document);

			// Iterate through each page of the PDF
			for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
				BufferedImage image = renderer.renderImageWithDPI(pageIndex, 300);
				List<String> qrCodeTexts = detectQRCodeWithRepair(image, pdfFile);

				for (String qrCodeText : qrCodeTexts) {

					if (isATInvoiceQRCode(qrCodeText)) {
						InvoiceMetadata invoiceMetadata = processQRCodeText(qrCodeText, batchProcessFile);
						ExtractedDataDTO extractedDataDTO = new ExtractedDataDTO();
						extractedDataDTO.setInvoiceMetadata(invoiceMetadata);
						extractedDataDTO.setIsExtractedData(true);

						extractedDataList.add(extractedDataDTO);
					}
					else {
						logger.warn("QR code text does not match AT invoice format: {}", qrCodeText);
						ExtractedDataDTO extractedDataDTO = new ExtractedDataDTO();
						extractedDataDTO.setInvoiceMetadata(generateDefaultInvoiceMetadata(pdfFile.getOriginalFilename()));
						extractedDataDTO.setIsExtractedData(true);
						extractedDataList.add(extractedDataDTO);

					}
				}
				logger.info("Detected {} QR codes from PDF file: {}", qrCodeTexts.size(), pdfFile.getOriginalFilename());

			}

			if (extractedDataList.isEmpty()) {

				logger.warn("No valid AT QR codes found in PDF file: {}", pdfFile.getOriginalFilename());
				ExtractedDataDTO defaultDTO = new ExtractedDataDTO();
				defaultDTO.setInvoiceMetadata(generateDefaultInvoiceMetadata(pdfFile.getOriginalFilename()));
				defaultDTO.setIsExtractedData(false);
			}

			/*
			if (!extractedDataDTOs.isEmpty()) {
				//return extractedDataDTOs;
			} else {
				logger.error("No QR codes found in PDF file: {}", pdfFile.getOriginalFilename());
				throw new NoQRCodeDetectedException("No QR codes found in PDF file: " + pdfFile.getOriginalFilename());
			}*/
		} catch (Exception e) {
			logger.warn("No QR codes found in PDF file: {}", pdfFile.getOriginalFilename());
			ExtractedDataDTO defaultDTO = new ExtractedDataDTO();
			defaultDTO.setInvoiceMetadata(generateDefaultInvoiceMetadata(pdfFile.getOriginalFilename()));
			defaultDTO.setIsExtractedData(false);
			String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
			logger.error("Error on processing the PDF. exception message: {}", rootCauseMessage, e);
		}
		return extractedDataList;
	}
	private InvoiceMetadata generateDefaultInvoiceMetadata(String originalFileName) {
		InvoiceMetadata invoiceMetadata = new InvoiceMetadata();
		invoiceMetadata.setIssuerVATNumber("");
		invoiceMetadata.setCompanyName("");
		invoiceMetadata.setAcquirerVATNumber("");
		invoiceMetadata.setSite("");
		invoiceMetadata.setPhoneNumber("");
		invoiceMetadata.setEmail("");
		invoiceMetadata.setAddress("");
		invoiceMetadata.setPostalCode("");
		invoiceMetadata.setAcquirerCountry("");
		invoiceMetadata.setInvoiceDate(java.time.LocalDate.now());
		invoiceMetadata.setInvoiceNumber("");
		invoiceMetadata.setDocumentPaidAt("");
		invoiceMetadata.setClient("");
		invoiceMetadata.setCurrency("");
		invoiceMetadata.setDueDate("");
		invoiceMetadata.setValueAddedTax("");
		invoiceMetadata.setSubtotal("");
		invoiceMetadata.setTotal("");
		invoiceMetadata.setPaymentStatus("");
		invoiceMetadata.setItems(new java.util.ArrayList<>());
		invoiceMetadata.setOriginalFileName(originalFileName);
		return invoiceMetadata;
	}
	/**
	 * Processes an image file to detect and handle QR codes.
	 *
	 * @param imageFile        The image file containing QR codes to process.
	 * @param batchProcessFile The BatchProcessFile object to store metadata.
	 * @return true if a QR code is found, false otherwise.
	 * @throws IOException If there's an error reading the image file.
	 */
	public List<ExtractedDataDTO> processImage(MultipartFile imageFile, BatchProcessFile batchProcessFile) throws IOException {
		BufferedImage image = ImageIO.read(imageFile.getInputStream());
		List<String> qrCodeTexts = detectQRCodeWithRepair(image, imageFile);
		List<ExtractedDataDTO> extractedDataList = new ArrayList<>();
		logger.info("Detected {} QR codes from image file: {}", qrCodeTexts.size(), imageFile.getOriginalFilename());
		if (!qrCodeTexts.isEmpty()) {
			for (String qrCodeText : qrCodeTexts) {
				if (isATInvoiceQRCode(qrCodeText)) {
					InvoiceMetadata invoiceMetadata = processQRCodeText(qrCodeText, batchProcessFile);
					ExtractedDataDTO extractedDataDTO = new ExtractedDataDTO();
					extractedDataDTO.setInvoiceMetadata(invoiceMetadata);
					extractedDataDTO.setIsExtractedData(true);
					extractedDataList.add(extractedDataDTO);
				}
				else
				{
					logger.warn("QR code text does not match AT invoice format: {}", qrCodeText);
					InvoiceMetadata invoiceMetadata = processQRCodeText(qrCodeText, batchProcessFile);
					ExtractedDataDTO extractedDataDTO = new ExtractedDataDTO();
					extractedDataDTO.setInvoiceMetadata(generateDefaultInvoiceMetadata(imageFile.getOriginalFilename()));
					extractedDataDTO.setIsExtractedData(true);
					extractedDataList.add(extractedDataDTO);
				}
			}
			logger.info("Extracted {} QR codes from image file: {}", extractedDataList.size(), imageFile.getOriginalFilename());

			if (extractedDataList.isEmpty()) {
				logger.warn("No valid AT QR codes found in image file: {}", imageFile.getOriginalFilename());
				ExtractedDataDTO defaultDTO = new ExtractedDataDTO();
				defaultDTO.setInvoiceMetadata(generateDefaultInvoiceMetadata(imageFile.getOriginalFilename()));
				defaultDTO.setIsExtractedData(false);

				//throw new NoQRCodeDetectedException("No valid QR code detected" + imageFile.getOriginalFilename());
			}

		}

		else {
			logger.error("No QR codes found in image file: {}", imageFile.getOriginalFilename());
			ExtractedDataDTO defaultDTO = new ExtractedDataDTO();
			defaultDTO.setInvoiceMetadata(generateDefaultInvoiceMetadata(imageFile.getOriginalFilename()));
			defaultDTO.setIsExtractedData(false);
			throw new NoQRCodeDetectedException("No QR codes found in image file: " + imageFile.getOriginalFilename());
		}
		return extractedDataList;
	}

	/**
	 * Processes the detected QR code text.
	 *
	 * @param qrCodeText       The QR code text detected.
	 * @param batchProcessFile The BatchProcessFile object to store metadata.
	 */
	private InvoiceMetadata processQRCodeText( String qrCodeText, BatchProcessFile batchProcessFile ) {
		InvoiceMetadata metadata = organizeAndLabelQRCodeData( qrCodeText, batchProcessFile.getComment(), batchProcessFile.getCostCenter() );  // Organize QR code data into metadata
		if ( metadata.getIssuerVATNumber() != null ) {
			String companyName = extractionService.fetchCompanyName( metadata.getIssuerVATNumber() );  // Use injected instance

			if ( companyName != null ) {
				metadata.setCompanyName( companyName );  // Set company name in metadata
			}
		}

		metadata.setOriginalFileName( batchProcessFile.getFilename() );
		batchProcessFile.setMetadata( metadata );
		batchProcessFileRepository.save( batchProcessFile );
		logger.info("Saving batch process file. id: {}", batchProcessFile.getId());
		return metadata;
	}

	private List<String> detectMultipleQRCodes(BufferedImage image, MultipartFile imageFile) {
		List<String> qrCodeTexts = new ArrayList<>();

		try {
			logger.info("*** Trying to detect QR codes with internal detector for image file: {}",
					imageFile.getOriginalFilename());

			LuminanceSource source = new BufferedImageLuminanceSource(image);
			BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
			Map<DecodeHintType, Object> hints = new HashMap<>();
			hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

			MultiFormatReader reader = new MultiFormatReader();
			Result[] results = new GenericMultipleBarcodeReader(reader).decodeMultiple(bitmap, hints);

			logger.info("Detected {} QR codes with internal detector for image file: {}", results != null ? results.length : 0, imageFile.getOriginalFilename());
			if (results != null) {
				for (Result result : results) {
					if (result.getBarcodeFormat().equals(BarcodeFormat.QR_CODE) && result.getText() != null) {
						qrCodeTexts.add(result.getText());
					}
				}
			}
		} catch (Exception e) {
			String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
			logger.error("Exception while detecting QR codes with internal detector for image: {} exception message: {}",
					imageFile.getOriginalFilename(), rootCauseMessage, e);
		}

		// If internal detector fails, try with external API
		if (qrCodeTexts.isEmpty()) {
			try {
				String qrCodeResult = "";//tryExternalDetector(image, imageFile);
				if (qrCodeResult != null) {
					qrCodeTexts.add(qrCodeResult);
				}
			} catch (Exception e) {
				String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
				logger.error("Exception while detecting QR codes with external API detector for image: {} exception message: {}",
						imageFile.getOriginalFilename(), rootCauseMessage, e);
			}
		}

		return qrCodeTexts;
	}


	/**
	 * Detects the first QR code in a BufferedImage.
	 *
	 * @param image The BufferedImage containing the QR code.
	 * @return The first detected QR code text, or null if no QR code is found.
	 */
	private String detectQRCode( BufferedImage image, MultipartFile imageFile ) {

		try {
			logger.info( "*** Trying to detect first QR code with internal detector for image file: {}", imageFile.getOriginalFilename() );
			LuminanceSource source = new BufferedImageLuminanceSource( image );  // Create luminance source from BufferedImage
			BinaryBitmap bitmap = new BinaryBitmap( new HybridBinarizer( source ) );  // Create binary bitmap from luminance source
			Map<DecodeHintType, Object> hints = new HashMap<>();  // Create hints for QR code decoding
			hints.put( DecodeHintType.TRY_HARDER, Boolean.TRUE );  // Use more intensive decoding methods
			Result result = new MultiFormatReader().decode( bitmap, hints );  // Decode QR code using hints

			if ( result != null && result.getBarcodeFormat().equals( BarcodeFormat.QR_CODE ) ) {
				if ( result.getText() != null ) {
					return result.getText();
				}
			}

		}
		catch ( Exception e ) {
			String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
			logger.error( "Exception while detecting QR code with internal detector for image: {} exception message: {}", imageFile.getOriginalFilename(), rootCauseMessage, e );
		}

		try {
			// Try with Decoder API
			logger.info( "*** No QR Code detected with internal detector for image file: {}", imageFile.getOriginalFilename() );
			String fileFormat = imageFile.getOriginalFilename().substring( imageFile.getOriginalFilename().lastIndexOf( '.' ) + 1 );

			Mono<String> qrCodeResult;
			if ( "pdf".equals( fileFormat ) ) {
				byte[] imageBytes = convertBufferedImageToByteArray( image, "jpeg" );
				MultipartFile multipartFile = convertToMultipartFile( imageBytes, imageFile.getOriginalFilename().substring( 0, imageFile.getOriginalFilename().lastIndexOf( '.' ) ), "image/jpeg" );
				qrCodeResult = barcodeQRCodeDecoderApiCaller.callDecodeApi( multipartFile );
			}
			else {
				qrCodeResult = barcodeQRCodeDecoderApiCaller.callDecodeApi( imageFile );
			}

			if ( nonNull( qrCodeResult ) ) {
				return qrCodeResult.block();
			}
		} catch ( DecoderApiNotFoundContentException e ){
			String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
			logger.error( "*** Decoder Api did not find any content for file: {} exception message: {}", imageFile.getOriginalFilename(), rootCauseMessage, e );
			return null;
		}
		catch ( DecoderApiInternalServerErrorException e ){
			String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
			logger.error( "*** Decoder Api internal server error occurred while detecting QR code with external API detector for file: {} exception message: {}", imageFile.getOriginalFilename(), rootCauseMessage, e );
			return null;
		}

		catch ( Exception e ) {
			String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
			logger.error( "Exception while detecting QR code with external API detector for file: {} exception message: {}", imageFile.getOriginalFilename(), rootCauseMessage, e );
		}

		logger.error( "No QR Code detected for file: {}", imageFile.getOriginalFilename() );
		return null;
	}

	/**
	 * Organizes QR code data into InvoiceMetadata object.
	 *
	 * @param qrCodeText The QR code text to organize.
	 * @return InvoiceMetadata object containing organized data.
	 */
	private InvoiceMetadata organizeAndLabelQRCodeData( String qrCodeText, String comment, String costCenter ) {
		String[] pairs = qrCodeText.split( "\\*" );  // Split QR code text into key-value pairs
		Map<String, String> dataMap = new HashMap<>();  // Create map to store key-value pairs

		// Parse key-value pairs from QR code text
		for ( String pair : pairs ) {
			String[] parts = pair.split( ":" );  // Split pair into key and value
			if ( parts.length == 2 ) {
				dataMap.put( parts[0], parts[1] );  // Store key-value pair in map
			}
		}

		InvoiceMetadata metadata = new InvoiceMetadata();
		// Set metadata fields from parsed data
		metadata.setIssuerVATNumber( dataMap.getOrDefault( "A", null ) );
		metadata.setAcquirerVATNumber( dataMap.getOrDefault( "B", null ) );
		metadata.setAcquirerCountry( dataMap.getOrDefault( "C", null ) );
		String invoiceDate = dataMap.getOrDefault( "F", null );
		if ( invoiceDate != null ) {
			metadata.setInvoiceDate( DateUtils.formatDate( invoiceDate )  );
		}
		metadata.setInvoiceNumber( dataMap.getOrDefault( "G", null ) );
		metadata.setAtcud( dataMap.getOrDefault( "H", null ) );
		metadata.setValueAddedTax( dataMap.getOrDefault( "N", null ) );
		metadata.setSubtotal( String.valueOf( fetchSubtotal(dataMap) ) );
		metadata.setTotal( dataMap.getOrDefault( "O", null ) );
		metadata.setComment( comment );
		metadata.setCostCenter( costCenter );
		return metadata;
	}

	private BigDecimal fetchSubtotal(Map<String, String> dataMap) {
		BigDecimal sum = BigDecimal.ZERO;
		for (String key : dataMap.keySet()) {
			if (key.equals( SubtotalField.I2.name() ) || key.equals(SubtotalField.I3.name()) || key.equals(SubtotalField.I5.name()) || key.equals(SubtotalField.I7.name())) {
				String value = dataMap.get(key);
				if (value != null) {
					try {
						BigDecimal price = new BigDecimal(value);
						sum = sum.add(price);
					} catch (NumberFormatException e) {
						String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
						logger.error("Error occurred during parsing value for Subtotal keys, key {} with value {}: {}", key, value, rootCauseMessage, e);
					}
				} else {
					logger.warn("Value for Subtotal key {} is null.", key);
				}
			}
		}
		return sum;
	}






	public byte[] convertBufferedImageToByteArray(BufferedImage image, String format) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(image, format, baos);
		return baos.toByteArray();
	}

	public MultipartFile convertToMultipartFile( byte[] imageBytes, String fileName, String contentType ) throws IOException {
		FileItem fileItem = new DiskFileItem( fileName, contentType, false, fileName, imageBytes.length, null );
		try ( ByteArrayInputStream inputStream = new ByteArrayInputStream( imageBytes ) ) {
			fileItem.getOutputStream().write( imageBytes );
		}
		return new CommonsMultipartFile( fileItem );
	}

	private List<String> detectQRCodeWithRepair(BufferedImage image, MultipartFile imageFile) {
	    List<String> qrCodeTexts = detectMultipleQRCodes(image, imageFile);
		for (String qrCodeText : qrCodeTexts) {
			if (!isATInvoiceQRCode(qrCodeText)) {
				logger.info("No QR codes detected. Repairing image with QRCodeRepairUtil.");
				BufferedImage repairedImage = QRCodeRepairUtil.repairQRCode(image);
				qrCodeTexts = detectMultipleQRCodes(repairedImage, imageFile);
			}
		}
	    return qrCodeTexts;
	}


}
