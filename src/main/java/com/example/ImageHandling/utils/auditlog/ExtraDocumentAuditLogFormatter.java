package com.example.ImageHandling.utils.auditlog;

import com.example.ImageHandling.domains.AuditLog;
import com.example.ImageHandling.domains.ExtraDocument;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 3/21/2025
 */

@Component
public class ExtraDocumentAuditLogFormatter implements AuditLogFormatter<ExtraDocument> {

	@Override
	public void formatAuditLog( AuditLog log, ExtraDocument extraDocument ) {
		log.setFileName( extraDocument.getFileName() );
		log.setExportedFileNames( Collections.singletonList( extraDocument.getFileName() ) );
		log.setFileType( extraDocument.getType() );
		log.setTimestamp( LocalDateTime.now() );
	}
}


