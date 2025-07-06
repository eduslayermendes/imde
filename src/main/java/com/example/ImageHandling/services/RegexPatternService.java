package com.example.ImageHandling.services;

import com.example.ImageHandling.domains.*;
import com.example.ImageHandling.domains.repository.InvoicesRepository;
import com.example.ImageHandling.domains.repository.RegexPatternRepository;
import com.example.ImageHandling.exception.DuplicateLayoutException;
import com.example.ImageHandling.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.example.ImageHandling.domains.types.LogOperation.PATTERN_CREATED;

/**
 * Service class for handling RegexPatterns and processing invoices.
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class RegexPatternService {

	private static final Logger logger = LoggerFactory.getLogger( RegexPatternService.class );

	private final RegexPatternRepository regexPatternRepository;

	private final InvoicesRepository invoicesRepository;

	private final AuditLogService auditLogService;

	private final ImageService imageService;

	private final IssuerService issuerService;

	private final ApplicationEventPublisher eventPublisher;

	private final AuthService authService;

	private final MongoTemplate mongoTemplate;

	public void deleteLayout( String id ) {
		logger.info( "Deleting RegexPattern with id: {}", id );
		Optional<RegexPattern> existingPatternOpt = regexPatternRepository.findById( id );
		if ( existingPatternOpt.isPresent() ) {
			RegexPattern existingPattern = existingPatternOpt.get();
			regexPatternRepository.deleteById( id );
			// Log delete action
			auditLogService.logChanges( "Pattern Deleted", authService.getLoggedInUserDetails().getUsername(), existingPattern, null, getPatternDetails( existingPattern ), null );
			eventPublisher.publishEvent( new RegexPatternDeletedEvent( this, id ) );
			logger.info( "Regex pattern deleted from InvoiceLayouts collection. id: {}", id );
		}
	}

	/**
	 * Saves a RegexPattern to the database.
	 *
	 * @param pattern The RegexPattern object to be saved.
	 * @return The saved RegexPattern object.
	 */
	@Transactional
	public RegexPattern createRegexPattern( RegexPattern pattern ) {
		if ( pattern == null || pattern.getName() == null || pattern.getName().isEmpty() ) {
			throw new IllegalArgumentException( "Pattern name must not be null or empty." );
		}
		Optional<RegexPattern> regexPattern = regexPatternRepository.findByName( pattern.getName() );
		if ( regexPattern.isPresent() ) {
			throw new DuplicateLayoutException( "Pattern with name " + pattern.getName() + " already exists." );
		}
		pattern.setCreatedAt( LocalDateTime.now() );
		RegexPattern createdPattern = regexPatternRepository.save( pattern );
		auditLogService.logChanges( PATTERN_CREATED.toString(), authService.getLoggedInUserDetails().getUsername(), null, createdPattern, null, getPatternDetails( createdPattern ) );
		return createdPattern;
	}

	/**
	 * Retrieves all RegexPatterns from the database.
	 *
	 * @return List of all RegexPattern objects.
	 */


	public List<RegexPattern> getAllPatternsWithPaginationSorting( Integer pageNo, Integer pageSize, Sort sort ) {
		logger.info( "Fetching all layout patterns with pagination and sorting - PageNo: {}, PageSize: {}", pageNo, pageSize );

		if ( sort == null || sort.isEmpty() ) {
			logger.warn( "Sort field is null or empty, defaulting to 'name' sorting" );
			sort = Sort.by( Sort.Order.asc( "name" ) );
		}
		Sort.Order order = sort.iterator().next();
		String sortField = order.getProperty();
		Sort.Direction sortDirection = order.getDirection();
		Pageable pageable = PageRequest.of( pageNo, pageSize, sort );

		if ( "name".equalsIgnoreCase( sortField ) ) {
			Query query = new Query().with( pageable );
			query.with( Sort.by( sortDirection.isAscending() ? Sort.Order.asc( "name" ) : Sort.Order.desc( "name" ) ) );
			query.collation( Collation.of( "en" ).strength( Collation.ComparisonLevel.secondary() ) ); // Case-insensitive sorting
			List<RegexPattern> layouts = mongoTemplate.find( query, RegexPattern.class );
			if ( layouts.isEmpty() ) {
				logger.info( "No layouts found for case-insensitive sorting by 'name'" );
			}
			return layouts;
		}
		else {
			Page<RegexPattern> pageResult = regexPatternRepository.findAll( pageable );
			if ( pageResult.hasContent() ) {
				return pageResult.getContent();
			}
			else {
				logger.info( "No layouts found for sorting by '{}'", sortField );
				return new ArrayList<>();
			}
		}

	}

	/**
	 * Retrieves the name of a RegexPattern by its ID.
	 *
	 * @param patternId The ID of the RegexPattern to retrieve the name for.
	 * @return The name of the RegexPattern.
	 */
	public String getNameByPatternId( String patternId ) {
		Optional<RegexPattern> optionalPattern = regexPatternRepository.findById( patternId );
		if ( optionalPattern.isPresent() ) {
			return optionalPattern.get().getName();
		}
		else {
			return ""; // or null, depending on your use case
		}
	}

	/**
	 * Checks if a RegexPattern exists with the given name.
	 *
	 * @param name The name of the RegexPattern to check.
	 * @return True if a RegexPattern with the name exists, false otherwise.
	 */
	public boolean existsPatternWithName( String name ) {
		return regexPatternRepository.existsByName( name );
	}

	/**
	 * Retrieves the Invoices entity by its ID.
	 *
	 * @param id The ID of the Invoices entity to retrieve.
	 * @return The Invoices entity if found, null otherwise.
	 */
	public Invoices getInvoiceFile( String id ) {
		return invoicesRepository.findById( id ).orElse( null );
	}

	/**
	 * Retrieves the ID of a RegexPattern by its name.
	 *
	 * @param patternName The name of the RegexPattern to retrieve the ID for.
	 * @return The ID of the RegexPattern, or null if not found.
	 */
	public String getPatternIdByName( String patternName ) {
		Optional<RegexPattern> optionalPattern = regexPatternRepository.findByName( patternName );
		logger.info( "Fetching layout pattern id for the '{}' layout", patternName );
		return optionalPattern.map( RegexPattern::getId ).orElseThrow( () -> new RuntimeException( "Pattern not found by name: " + patternName ) );
	}

	public List<RegexPattern> getPatternsByIssuerId( String issuerId ) throws NoSuchFieldException {
		logger.info( "Fetching layout patterns for the issuer id {}", issuerId );
		List<RegexPattern> regexPatterns = new ArrayList<>();
		Optional<Issuer> issuer = issuerService.getIssuerById( issuerId );
		if ( issuer.isEmpty() ) {
			logger.error( "Issuer not found for id {}", issuerId );
			throw new NoSuchFieldException( "No issuer found for this id: " + issuerId );
		}
		issuer.ifPresent( value -> value.getLayoutIds().forEach( layoutId -> {
			Optional<RegexPattern> optionalPattern = regexPatternRepository.findById( layoutId );
			optionalPattern.ifPresent( regexPatterns::add );
		} ) );
		if ( regexPatterns.isEmpty() ) {
			logger.error( "No layout patterns found for issuer id {}", issuerId );
			throw new NoSuchFieldException( "No patterns found for this issuer" );
		}
		return regexPatterns;
	}

	public RegexPattern updatePattern( String id, RegexPattern pattern ) {
		logger.info( "Updating layout pattern configuration for the id {}", id );
		Optional<RegexPattern> existingRegexPatternOpt = regexPatternRepository.findById( id );
		if ( existingRegexPatternOpt.isPresent() ) {
			RegexPattern existingRegexPattern = existingRegexPatternOpt.get();

			if ( pattern.getName() != null ) {
				existingRegexPattern.setName( pattern.getName() );
			}
			if ( pattern.getFieldMappings() != null ) {
				existingRegexPattern.setFieldMappings( pattern.getFieldMappings() );
			}
			if ( pattern.getLanguage() != null ) {
				existingRegexPattern.setLanguage( pattern.getLanguage() );
			}
			if ( pattern.getDateFormat() != null ) {
				existingRegexPattern.setDateFormat( pattern.getDateFormat() );
			}
			if ( pattern.getCreatedBy() != null ) {
				existingRegexPattern.setCreatedBy( pattern.getCreatedBy() );
			}

			existingRegexPattern.setUpdatedAt( LocalDateTime.now() );
			logger.info( "Updated layout pattern configuration for the id {}", id );
			return regexPatternRepository.save( existingRegexPattern );
		}
		else {
			logger.error( "Layout pattern not found. for the id {}", id );
			throw new RuntimeException( "PATTERN not found" );
		}
	}

	public String formatDate( String dateStr, String formatDate ) {
		logger.info( "Formatting date string: {} with format: {}", dateStr, formatDate );
		return DateUtils.formatDate( dateStr, formatDate );
	}

	private List<String> getPatternDetails( RegexPattern pattern ) {
		// Helper method to get pattern details as a list of strings
		List<String> details = new ArrayList<>();
		details.add( "Name: " + pattern.getName() );
		details.add( "Language: " + pattern.getLanguage() );
		details.add( "Created By: " + pattern.getCreatedBy() );

		// Convert each FieldMapping to its string representation
		if ( pattern.getFieldMappings() != null && !pattern.getFieldMappings().isEmpty() ) {
			List<String> fieldMappingDetails = new ArrayList<>();
			for ( FieldMapping fieldMapping : pattern.getFieldMappings() ) {
				fieldMappingDetails.add( fieldMapping.toString() );
			}
			details.add( "FieldMappings: " + String.join( ", ", fieldMappingDetails ) );
		}
		else {
			details.add( "FieldMappings: None" );
		}

		return details;
	}

	public String extractTextFromImage( MultipartFile imageFile, String language ) throws TesseractException, IOException {
		logger.info( "Extracting text from image file" );
		Mat image = Imgcodecs.imdecode( new MatOfByte( imageFile.getBytes() ), Imgcodecs.IMREAD_COLOR );
		return imageService.extractText( image, language );
	}

	public long getLayoutsCount() {
			logger.info( "Layouts count: {}", regexPatternRepository.count() );
			return regexPatternRepository.count();

	}

	public Optional<RegexPattern> getPatternById( String id ) {
		logger.info( "Fetching layout pattern by id {}", id );
		return regexPatternRepository.findById( id );

	}
}


