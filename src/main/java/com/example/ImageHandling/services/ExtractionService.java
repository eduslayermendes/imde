package com.example.ImageHandling.services;

import com.example.ImageHandling.domains.dto.ExtractedDataDTO;
import com.example.ImageHandling.exception.MetaDataNotFoundException;
import com.example.ImageHandling.infrastructure.soap.checkvat.CheckVat;
import com.example.ImageHandling.infrastructure.soap.checkvat.CheckVatPortType;
import com.example.ImageHandling.infrastructure.soap.checkvat.CheckVatService;
import com.example.ImageHandling.domains.*;
import com.example.ImageHandling.exception.IllegalDataException;
import com.example.ImageHandling.domains.repository.InvoicesRepository;
import com.example.ImageHandling.domains.repository.RegexPatternRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Holder;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.ImageHandling.utils.DateUtils.formatDate;
import static java.util.Objects.isNull;

@Slf4j
@Service
public class ExtractionService {

	private static final Logger logger = LoggerFactory.getLogger(ExtractionService.class);

	private final RegexPatternRepository regexPatternRepository;

	private final PDFTextStripper pdfTextStripper;

	private final static String PT_PREFIX = "PT";

	String test;

	private final InvoicesRepository invoicesRepository; // Add this field

	@Autowired
	public ExtractionService( RegexPatternRepository regexPatternRepository,
		InvoicesRepository invoicesRepository ) throws IOException {
		this.regexPatternRepository = regexPatternRepository;
		this.pdfTextStripper = new PDFTextStripper();
		this.invoicesRepository = invoicesRepository; // Assign here
	}

	/**
	 * Extracts plain text content from a PDF file.
	 *
	 * @param pdfFile The MultipartFile containing the PDF file.
	 * @return Plain text content extracted from the PDF.
	 * @throws IOException If there's an error reading the PDF file.
	 */
	public String extractTextFromPDF( MultipartFile pdfFile ) throws IOException {
		logger.info( "Extracting text from PDF..." );
		try ( PDDocument document = PDDocument.load( pdfFile.getInputStream() ) ) {
			return pdfTextStripper.getText( document );
		}
	}

	/**
	 * Extracts metadata from a PDF file based on provided field mappings.
	 *
	 * @param file          The MultipartFile containing the PDF file.
	 * @param fieldMappings List of FieldMapping objects specifying regex patterns for extraction.
	 * @return Extracted InvoiceMetadata object.
	 * @throws IOException If there's an error reading the PDF file.
	 */
	public InvoiceMetadata extractTextFromPDFWithMappings( MultipartFile file, List<FieldMapping> fieldMappings ) throws IOException {
		try ( PDDocument document = PDDocument.load( file.getInputStream() ) ) {
			String text = pdfTextStripper.getText( document );
			InvoiceMetadata invoiceMetadata = populateInvoiceMetadata( text, fieldMappings );
			if ( invoiceMetadata == null || invoiceMetadata.isEmpty() ) {
				logger.error( "Error on extracting the data from the PDF with Layout fields, No metadata found based on the layout configs in the file: {}", file.getOriginalFilename() );
				// Return default InvoiceMetadata with empty fields instead of throwing
				return generateDefaultInvoiceMetadata(file.getOriginalFilename());
			}
			invoiceMetadata.setOriginalFileName( file.getOriginalFilename() );
			return invoiceMetadata;
		}
	}

	/**
	 * Extracts and labels metadata from a plain text string using a specified pattern ID.
	 *
	 * @param text      The plain text content to extract and label.
	 * @param patternId The ID of the pattern to use for text labeling.
	 * @return String representation of the labeled metadata.
	 */
	public InvoiceMetadata extractAndLabelText( String text, String patternId ) {
		Optional<RegexPattern> optionalPattern = regexPatternRepository.findById( patternId );
		test = patternId;
		if ( optionalPattern.isPresent() ) {
			List<FieldMapping> fieldMappings = optionalPattern.get().getFieldMappings();
			InvoiceMetadata invoiceMetadata = populateInvoiceMetadata( text, fieldMappings );
			if (invoiceMetadata == null || invoiceMetadata.isEmpty()) {
				return generateDefaultInvoiceMetadata("");
			}
			return invoiceMetadata;
		}
		else {
			return generateDefaultInvoiceMetadata("");
		}
	}

	/**
	 * Populates InvoiceMetadata object based on extracted text and field mappings.
	 *
	 * @param text          The plain text content to extract metadata from.
	 * @param fieldMappings List of FieldMapping objects specifying regex patterns for metadata extraction.
	 * @return Populated InvoiceMetadata object.
	 */
	public InvoiceMetadata populateInvoiceMetadata( String text, List<FieldMapping> fieldMappings ) {
		InvoiceMetadata invoiceMetadata = new InvoiceMetadata();
		List<Item> items = new ArrayList<>();
		for ( FieldMapping fieldMapping : fieldMappings ) {
			// Ensure the regex includes a capturing group for the desired value
			String regex = fieldMapping.getRegex();
			Pattern compiledPattern = Pattern.compile( regex );
			Matcher matcher = compiledPattern.matcher( text );
			if ( matcher.find() ) {
				String extractedValue = matcher.group( 1 ); // Use the first capturing group

				switch ( fieldMapping.getName() ) {
					case "Issuer VAT number":
						String issVat = extractedValue;
						if ( issVat.startsWith( "PT" ) ) {
							issVat = issVat.substring( 2 );
						}
						invoiceMetadata.setIssuerVATNumber( issVat );
						String companyName = fetchCompanyName( issVat );
						invoiceMetadata.setCompanyName( companyName );
						break;
					case "Acquirer VAT number":
						String acVat = extractedValue;
						if ( acVat.startsWith( "PT" ) ) {
							acVat = acVat.substring( 2 );
						}
						invoiceMetadata.setAcquirerVATNumber( acVat );
						break;
					case "Company Name":
						invoiceMetadata.setCompanyName( extractedValue );
						break;
					case "Site":
						invoiceMetadata.setSite( extractedValue );
						break;
					case "Phone Number":
						invoiceMetadata.setPhoneNumber( extractedValue );
						break;
					case "E-mail":
						invoiceMetadata.setEmail( extractedValue );
						break;
					case "Address":
						invoiceMetadata.setAddress( extractedValue );
						break;
					case "Postal Code":
						invoiceMetadata.setPostalCode( extractedValue );
						break;
					case "Acquirer country":
						invoiceMetadata.setAcquirerCountry( extractedValue );
						break;
					case "Invoice Date":
						Optional<RegexPattern> optionalPattern = regexPatternRepository.findById( test );
						if (optionalPattern.isPresent() && "Invoice Date".equals(fieldMapping.getName())) {
							String formattedDate = formatDate(extractedValue, optionalPattern.get().getDateFormat());
							DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
							LocalDate localDate = LocalDate.parse(formattedDate, formatter);
							invoiceMetadata.setInvoiceDate(localDate);
						}
						break;
					case "Invoice Number":
						invoiceMetadata.setInvoiceNumber( extractedValue );
						break;
					case "Document paid at":
						invoiceMetadata.setDocumentPaidAt( extractedValue );
						break;
					case "Client":
						invoiceMetadata.setClient( extractedValue );
						break;
					case "Currency":
						invoiceMetadata.setCurrency( extractedValue );
						break;
					case "Due Date":
						invoiceMetadata.setDueDate( extractedValue );
						break;
					case "Value-Added Tax":
						invoiceMetadata.setValueAddedTax( extractedValue );
						break;
					case "Subtotal":
						invoiceMetadata.setSubtotal( extractedValue );
						break;
					case "Total":
						invoiceMetadata.setTotal( extractedValue );
						break;
					case "Payment Status":
						invoiceMetadata.setPaymentStatus( extractedValue );
						break;
					// Handle items
					case "Item Quantity":
					case "Item Value":
					case "Item Subtotal":
					case "Total Amount":
					case "Article Ref":
					case "Item Name":
						Item item = new Item();
						switch ( fieldMapping.getName() ) {
							case "Item Quantity":
								item.setItemQuantity( extractedValue );
								break;
							case "Item Value":
								item.setItemValue( extractedValue );
								break;
							case "Item Subtotal":
								item.setItemSubtotal( extractedValue );
								break;
							case "Total Amount":
								item.setTotalAmount( extractedValue );
								break;
							case "Article Ref":
								item.setArticleRef( extractedValue );
								break;
							case "Item Name":
								item.setItemName( extractedValue );
								break;
						}
						items.add( item );
						break;
				}
			}
		}

		if ( !items.isEmpty() ) {
			invoiceMetadata.setItems( items );
		}
		// Return default if empty
		return invoiceMetadata.isEmpty() ? generateDefaultInvoiceMetadata("") : invoiceMetadata;
	}

	/**
	 * Saves an Invoices entity with the uploaded file details.
	 *
	 * @param file The MultipartFile containing the invoice file.
	 * @return The Invoices entity.
	 * @throws IOException If there's an error reading the file content.
	 */
	public Invoices createInvoiceFromFile( MultipartFile file ) throws IOException {
		return Invoices.builder()
			.fileName( file.getOriginalFilename() )
			.fileContent( file.getBytes() )
			.fileType( file.getContentType() )
			.createdAt( LocalDateTime.now() )
			.build();
	}

	/**
	 * Extracts text from a PDF file using a specified pattern ID to map fields.
	 *
	 * @param file      The MultipartFile containing the PDF file.
	 * @param patternId The ID of the pattern to use for text extraction.
	 * @return String representation of the extracted metadata.
	 * @throws IOException If there's an error reading the PDF file.
	 */
	public ExtractedDataDTO extractTextFromPDFWithPattern( MultipartFile file, String patternId ) throws IOException {
		ExtractedDataDTO extractedDataDTO = new ExtractedDataDTO();
		if ( isNull( patternId ) || patternId.isEmpty() ) {
			throw new IllegalDataException( "Pattern Id is empty" );
		}

			Optional<RegexPattern> optionalPattern = regexPatternRepository.findById( patternId );
			test = patternId;
			if ( optionalPattern.isPresent() ) {
				List<FieldMapping> fieldMappings = optionalPattern.get().getFieldMappings();
				Invoices invoice = createInvoiceFromFile( file );
				InvoiceMetadata invoiceMetadata = extractTextFromPDFWithMappings( file, fieldMappings );
				invoice.setInvoiceMetadata( invoiceMetadata );
				extractedDataDTO.setInvoiceMetadata( invoiceMetadata );
				extractedDataDTO.setIsExtractedData( true );
				return extractedDataDTO;
			}
			else {
				logger.error( "Error on extracting the data, Pattern not found for id: {}", patternId );
				throw new FileNotFoundException( "Pattern not found for id: " + patternId );
			}

	}

	/**
	 * Fetches company name using VAT number.
	 * <p>
	 * First, it checks if any invoice with the given VAT number with the external service
	 * If not, it attempts to fetch the company name through the database.
	 * </p>
	 *
	 * @param issuerNif The VAT number of the issuer.
	 * @return Company name associated with the VAT number, or an empty string if not found.
	 */
	public String fetchCompanyName( String issuerNif ) {
		// Remove PT prefix if present
		String normalizedNif = issuerNif;
		if (normalizedNif.startsWith(PT_PREFIX)) {
			normalizedNif = normalizedNif.substring(2);
		}



		CheckVatService service = new CheckVatService();
		CheckVatPortType port = service.getCheckVatPort();
		CheckVat request = new CheckVat();
		request.setCountryCode(PT_PREFIX);
		request.setVatNumber(normalizedNif);

		Holder<String> countryCodeHolder = new Holder<>(request.getCountryCode());
		Holder<String> vatNumberHolder = new Holder<>(request.getVatNumber());
		Holder<XMLGregorianCalendar> requestDateHolder = new Holder<>();
		Holder<Boolean> validHolder = new Holder<>();
		Holder<String> nameHolder = new Holder<>();
		Holder<String> addressHolder = new Holder<>();

		try {
			port.checkVat(countryCodeHolder, vatNumberHolder, requestDateHolder, validHolder, nameHolder, addressHolder);
			if (nameHolder.value != null) {
				logger.info("Company name found for VAT number: {}", normalizedNif);
				return nameHolder.value;
			} else {
				logger.error("Company name not found for VAT number: {}", normalizedNif);
				return "";
			}
		} catch (Exception e) {
			logger.error("Error fetching company name for VAT number: {}. Exception: {}", normalizedNif, ExceptionUtils.getStackTrace(e));
			// 2. Fallback to DB
			List<Invoices> invoices = invoicesRepository.findByIssuerVATNumberAndCompanyNameNotEmpty(normalizedNif);
			for (Invoices invoice : invoices) {
				String existingCompanyName = invoice.getInvoiceMetadata().getCompanyName();
				if (existingCompanyName != null && !existingCompanyName.trim().isEmpty()) {
					return existingCompanyName;
				}
				else {
					logger.warn("No valid company name found for invoice with VAT number: {}", normalizedNif);
					return "";
				}
			}
			logger.error("No invoices found for VAT number: {}", normalizedNif);
			return "";
		}



    }

	/**
	 * Generates a default InvoiceMetadata object with all fields set to empty strings,
	 * except for invoiceDate (set to now) and items (empty list).
	 */
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


}
