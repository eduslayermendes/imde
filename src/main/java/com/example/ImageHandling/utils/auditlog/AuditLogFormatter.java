package com.example.ImageHandling.utils.auditlog;

import com.example.ImageHandling.domains.AuditLog;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 3/21/2025
 */
public interface AuditLogFormatter <T> {
	void formatAuditLog ( AuditLog log, T object );

}
