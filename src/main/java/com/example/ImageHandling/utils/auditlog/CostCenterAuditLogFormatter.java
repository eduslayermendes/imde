package com.example.ImageHandling.utils.auditlog;

import com.example.ImageHandling.domains.AuditLog;
import com.example.ImageHandling.domains.CostCenter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 3/24/2025
 */
@Component
public class CostCenterAuditLogFormatter implements AuditLogFormatter<CostCenter> {

	@Override
	public void formatAuditLog( AuditLog log, CostCenter costCenter ) {
		log.setTimestamp( LocalDateTime.now() );
		log.setFileName( costCenter.getName() );
		log.setFileType( "Cost Center" );

	}
}
