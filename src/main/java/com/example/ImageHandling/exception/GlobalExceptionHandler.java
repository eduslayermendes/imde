package com.example.ImageHandling.exception;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 10/14/2024
 */
import com.example.ImageHandling.domains.dto.ErrorResponse;
import com.example.ImageHandling.utils.ResponseUtil;
import com.mongodb.MongoSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.UncategorizedMongoDbException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(UncategorizedMongoDbException.class)
	public ResponseEntity<ErrorResponse> handleMongoException(UncategorizedMongoDbException ex) {
		logger.error("MongoDB error occurred:", ex);
		return ResponseUtil.createErrorResponse(
			ex,
			HttpStatus.INTERNAL_SERVER_ERROR,
			"MongoDB Error"
		);
	}

	@ExceptionHandler(MongoSecurityException.class)
	public ResponseEntity<ErrorResponse> handleMongoSecurityException(MongoSecurityException ex) {
		logger.error("MongoDB Security Error occurred:", ex);
		return ResponseUtil.createErrorResponse(
			ex,
			HttpStatus.UNAUTHORIZED,
			"MongoDB Security Error"
		);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
		logger.error("Illegal argument error occurred:", ex);
		return ResponseUtil.createErrorResponse(
			ex,
			HttpStatus.BAD_REQUEST,
			"Illegal Argument"
		);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex, WebRequest request) {
		logger.error("Unhandled general exception occurred:", ex);
		return ResponseUtil.createErrorResponse(
			ex,
			HttpStatus.INTERNAL_SERVER_ERROR,
			"An unexpected error occurred"
		);
	}

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ErrorResponse> handleAllRunTimeExceptions(Exception ex, WebRequest request) {
		logger.error("Runtime exception occurred:", ex);
		return ResponseUtil.createErrorResponse(
			ex,
			HttpStatus.INTERNAL_SERVER_ERROR,
			"An unexpected error occurred"
		);
	}

	@ExceptionHandler(NoSuchFieldException.class)
	public ResponseEntity<ErrorResponse> handleNoSuchFieldException(NoSuchFieldException ex) {
		logger.error("No such field error occurred:", ex);
		return ResponseUtil.createErrorResponse(
			ex,
			HttpStatus.BAD_REQUEST,
			"No such field"
		);
	}


	@ExceptionHandler( MultipartException.class)
	public ResponseEntity<ErrorResponse> handleMultipartException(MultipartException ex){
		logger.error("Multipart file exception occurred:", ex);
		return ResponseUtil.createErrorResponse(
			ex,
			HttpStatus.BAD_REQUEST,
			"Invalid multipart file"
		);
	}

	@ExceptionHandler( AccessDeniedException.class )
	public ResponseEntity<ErrorResponse> handleAccessDeniedException(Exception ex, WebRequest request) {
		logger.error("Access denied error occurred:", ex);
		return ResponseUtil.createErrorResponse(
			ex,
			HttpStatus.FORBIDDEN,
			"Access denied! you do not have the required permission" );
	}

	@ExceptionHandler( DuplicateInvoiceException.class )
	public ResponseEntity<ErrorResponse> handleDuplicateInvoiceException(Exception ex) {
		return ResponseUtil.createErrorResponse(
			ex,
			HttpStatus.CONFLICT,
			"Duplicate invoice detected" );
	}

	@ExceptionHandler( MetaDataNotFoundException.class )
	public ResponseEntity<ErrorResponse> handleMetaDataNotFoundException(Exception ex) {
		return ResponseUtil.createErrorResponse(
			ex,
			HttpStatus.NOT_FOUND,
			"No metadata found based on the layout configs" );
	}

	@ExceptionHandler( CustomTimeoutException.class)
	public ResponseEntity<ErrorResponse> handleCustomTimeoutException(Exception ex) {
		return ResponseUtil.createErrorResponse(
			ex,
			HttpStatus.REQUEST_TIMEOUT,
			"Request timed out" );
	}

}
