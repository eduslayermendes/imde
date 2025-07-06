package com.example.ImageHandling.services;

import com.example.ImageHandling.domains.ExtraDocumentType;
import com.example.ImageHandling.domains.repository.ExtraDocumentTypeRepository;
import com.example.ImageHandling.domains.types.LogOperation;
import com.example.ImageHandling.exception.DuplicateExtraDocumentTypeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.nonNull;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 3/12/2025
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExtraDocumentTypeService {
	private final AuthService authService;
	private final ExtraDocumentTypeRepository extraDocumentTypeRepository;
	private static final Logger logger = LoggerFactory.getLogger( ExtraDocumentTypeService.class );

	private final AuditLogService auditLogService;

	public ExtraDocumentType createDocumentType( ExtraDocumentType extraDocumentType ) {
		log.info("Attempting to create extra document type: {}", extraDocumentType);

		ExtraDocumentType savedDocumentType = extraDocumentTypeRepository.save( extraDocumentType );
		if ( savedDocumentType == null ) {
			throw new IllegalArgumentException( "Failed to create extra document type" );
		}
		logger.info( "Created extra document type successfully: {}", savedDocumentType );
		auditLogService.persistAuditLog( savedDocumentType, LogOperation.EXTRA_DOCUMENT_TYPE_CREATED );
		return savedDocumentType;
	}

	public List<ExtraDocumentType> findAll() {
		log.info( "Fetching all extra document types");
		return extraDocumentTypeRepository.findAll();
	}

	public void deleteDocumentType( String id ) {
		log.info("Attempting to delete extra document type with id: {}", id);
		ExtraDocumentType foundDocumentType = extraDocumentTypeRepository.findById( id ).orElseThrow( () ->
			new IllegalArgumentException( "Extra document type not found for id: " + id ) );
		extraDocumentTypeRepository.deleteById( id );
		auditLogService.persistAuditLog( foundDocumentType, LogOperation.EXTRA_DOCUMENT_TYPE_DELETED );
	}

	public Optional<ExtraDocumentType> findById( String id ) {
		logger.info("Fetching extra document type with id: {}", id);
		return extraDocumentTypeRepository.findById( id );
	}

	public ExtraDocumentType saveOrUpdate( ExtraDocumentType extraDocumentType ) {
		logger.info("Saving extra document type: {}", extraDocumentType);

		if ( extraDocumentType.getId() != null ) {
			Optional<ExtraDocumentType> foundDocumentTypeOpt = extraDocumentTypeRepository.findById( extraDocumentType.getId() );
			if ( foundDocumentTypeOpt.isPresent() ) {
				ExtraDocumentType existingDocumentType = foundDocumentTypeOpt.get();
				existingDocumentType.setDescription( extraDocumentType.getDescription() );
				existingDocumentType.setActive( extraDocumentType.getActive() );
				ExtraDocumentType updatedDocumentType = extraDocumentTypeRepository.save( existingDocumentType );
				auditLogService.persistAuditLog( updatedDocumentType, LogOperation.EXTRA_DOCUMENT_TYPE_UPDATED );
				logger.info("Updated extra document type successfully: {}", updatedDocumentType);
				return updatedDocumentType;
			} else {
				throw new IllegalArgumentException("Extra document type with id " + extraDocumentType.getId() + " not found");
			}
		} else {

			Optional.ofNullable( extraDocumentType.getName() ).ifPresent( name -> {
				extraDocumentTypeRepository.findByName( name.trim() ).ifPresent( foundDocumentType -> {
					throw new DuplicateExtraDocumentTypeException( "Extra document type with name " + name + " already exists" );
				});
			});

			extraDocumentTypeRepository.save( extraDocumentType );
			auditLogService.persistAuditLog( extraDocumentType, LogOperation.EXTRA_DOCUMENT_TYPE_CREATED );
			logger.info("Saved extra document type successfully: {}", extraDocumentType);
			return extraDocumentType;
		}
	}





}
