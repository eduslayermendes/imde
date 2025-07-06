package com.example.ImageHandling.utils.auditlog;

import com.example.ImageHandling.domains.AuditLog;
import com.example.ImageHandling.domains.ExtraDocumentType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 3/24/2025
 */
@Component
public class ExtraDocumentTypeAuditLogFormatter implements AuditLogFormatter<ExtraDocumentType> {

	@Override public void formatAuditLog( AuditLog log, ExtraDocumentType extraDocumentType ) {
		log.setTimestamp( LocalDateTime.now() );
		log.setFileName( extraDocumentType.getName() );
		log.setFileType( "Extra Document Type" );
	}
}
