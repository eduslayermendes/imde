package com.example.ImageHandling.utils.auditlog;

import com.example.ImageHandling.domains.AuditLog;
import com.example.ImageHandling.domains.Invoices;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 3/21/2025
 */

@Component
public class InvoiceAuditLogFormatter implements AuditLogFormatter<Invoices> {

	@Override
	public void formatAuditLog( AuditLog log, Invoices invoice ) {
		log.setFileName( invoice.getFileName() );
		log.setFileType( "Invoice" );
		log.setInvoiceMetadata( invoice.getInvoiceMetadata().getInvoiceNumber() );
		log.setTimestamp( LocalDateTime.now() );

	}
}

