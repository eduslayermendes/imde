package com.example.ImageHandling.utils.auditlog;

import com.example.ImageHandling.domains.AuditLog;
import com.example.ImageHandling.domains.Issuer;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 3/21/2025
 */
@Component
public class IssuerAuditLogFormatter implements AuditLogFormatter<Issuer> {
	@Override
	public void formatAuditLog( AuditLog log, Issuer issuer) {
		log.setFileName( issuer.getName() );
		log.setFileType( "Issuer" );
		log.setTimestamp( LocalDateTime.now() );
	}
}

