// AuditLogService.java
package com.example.ImageHandling.services;

import com.example.ImageHandling.domains.*;
import com.example.ImageHandling.domains.types.LogAction;
import com.example.ImageHandling.domains.repository.AuditLogRepository;
import com.example.ImageHandling.domains.types.LogOperation;
import com.example.ImageHandling.utils.auditlog.AuditLogFormatter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.formula.functions.T;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.ParameterizedType;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogRepository auditLogRepository;
    private final Map< Class<?>, AuditLogFormatter<?>> formatterRegistry = new HashMap<>();

    private AuthService authService;

    @Autowired
    public AuditLogService(List<AuditLogFormatter<?>> formatters, AuditLogRepository auditLogRepository, AuthService authService ) {
        this.auditLogRepository = auditLogRepository;
        formatters.forEach(formatter -> {
            Class<?> type = (Class<?>) ((ParameterizedType) formatter.getClass()
                .getGenericInterfaces()[0]).getActualTypeArguments()[0];
            formatterRegistry.put(type, formatter);
        });
        this.authService = authService;
    }


    public <T> void persistAuditLog(T object, LogOperation operation) {
        String username = Optional.ofNullable(authService.getLoggedInUserDetails().getUsername())
            .orElse(AuthService.UNKNOWN_USER);
        logger.info("Persisting audit log for operation: {}, user: {}", operation, username);

        AuditLog log = new AuditLog();
        log.setTimestamp(LocalDateTime.now());
        log.setOperation(operation.toString());
        log.setUsername(username);

        try {
            AuditLogFormatter<T> formatter = (AuditLogFormatter<T>) formatterRegistry.get(object.getClass());
            if (formatter == null) {
                logger.error("No AuditLogFormatter registered for type: {}", object.getClass().getName());
                return;
            }
            formatter.formatAuditLog(log, object);

            auditLogRepository.save(log);
            logger.info("Audit log persisted successfully for user: {} and operation: {}", username, operation);

        } catch (Exception e) {
            logger.error("Error while persisting audit log for operation: {}, user: {}. Error: {}",
                operation, username, e.getMessage(), e);
        }
    }



    @Transactional
    public void logUpload(String username, String fileName) {
        logger.debug("Logging upload action for user: {} and file: {}", username, fileName);
        logAction( LogAction.UPLOAD.toString(), username, fileName);
    }

    public void logSubmit(String username, String fileName) {
        logger.debug("Logging submit action for user: {} and file: {}", username, fileName);
        logAction(LogAction.SUBMIT.toString(), username, fileName);
    }

    @Transactional
    public void logAction(String operation, String username, String fileName) {
        AuditLog log = new AuditLog();
        log.setOperation(operation);
        log.setUsername(username);
        log.setFileName(fileName);
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);
    }


    public void logChanges(String operation, String username, RegexPattern oldPattern, RegexPattern newPattern, List<String> oldDetails, List<String> newDetails) {
        logger.debug("Logging changes for operation: {}, user: {}", operation, username);
        AuditLog log = new AuditLog();
        log.setOperation(operation);
        log.setUsername(username);
        log.setFileName("Layout");
        log.setTimestamp(LocalDateTime.now());

        String oldState = patternToString(oldPattern, oldDetails != null ? oldDetails : Collections.emptyList());
        String newState = patternToString(newPattern, newDetails != null ? newDetails : Collections.emptyList());

        log.setFileContent("Old State:\n" + oldState + "\n\nNew State:\n" + newState);
        auditLogRepository.save(log);
    }

    private String patternToString(RegexPattern pattern, List<String> details) {
        if (pattern == null) {
            return "No pattern available";
        }
        return "ID: " + pattern.getId() + ", " + String.join(", ", details);
    }


    public void logChanges(String operation, String username, Issuer oldIssuer, Issuer newIssuer, List<String> oldLayoutNames, List<String> newLayoutNames) {
        AuditLog log = new AuditLog();
        log.setOperation(operation);
        log.setUsername(username);
        log.setFileName("Issuer Changes");
        log.setTimestamp(LocalDateTime.now());

        String oldState = issuerToString(oldIssuer, oldLayoutNames);
        String newState = issuerToString(newIssuer, newLayoutNames);

        log.setFileContent("Old State:\n" + oldState + "\n\nNew State:\n" + newState);
        auditLogRepository.save(log);
    }

    private String issuerToString(Issuer issuer, List<String> layoutNames) {
        return "Name: " + issuer.getName() + ", isPt: " + issuer.isPt() + ", Layouts: " + layoutNames;
    }


    public void logInvoiceChanges(String operation, String username, Invoices oldInvoice, Invoices newInvoice) {
        AuditLog log = new AuditLog();
        log.setOperation(operation);
        log.setUsername(username);
        log.setFileName("Invoice Metadata Changes");
        log.setTimestamp(LocalDateTime.now());

        String oldState = invoiceToString(oldInvoice);
        String newState = invoiceToString(newInvoice);

        log.setFileContent("Old State:\n" + oldState + "\n\nNew State:\n" + newState);
        auditLogRepository.save(log);
    }

    private String invoiceToString(Invoices invoice) {
        if (invoice == null) {
            return "No invoice available";
        }
        return "ID: " + invoice.getInvoiceid() + ", Metadata: " + invoice.getInvoiceMetadata();
    }


    public void logInvoiceMetadataChanges(String operation, String username, InvoiceMetadata oldMetadata, InvoiceMetadata newMetadata) {
        AuditLog log = new AuditLog();
        log.setOperation(operation);
        log.setUsername(username);
        log.setFileName("Invoice Metadata Changes");
        log.setTimestamp(LocalDateTime.now());

        String oldState = metadataToString(oldMetadata);
        String newState = metadataToString(newMetadata);

        log.setFileContent("Old State:\n" + oldState + "\n\nNew State:\n" + newState);
        auditLogRepository.save(log);
    }


    private String metadataToString(InvoiceMetadata metadata) {
        if (metadata == null) {
            return "No metadata available";
        }
        StringBuilder sb = new StringBuilder();
        appendDetail(sb, "Issuer VAT Number", metadata.getIssuerVATNumber());
        appendDetail(sb, "Acquirer VAT Number", metadata.getAcquirerVATNumber());
        appendDetail(sb, "Company Name", metadata.getCompanyName());
        appendDetail(sb, "Site", metadata.getSite());
        appendDetail(sb, "Phone Number", metadata.getPhoneNumber());
        appendDetail(sb, "Email", metadata.getEmail());
        appendDetail(sb, "Postal Code", metadata.getPostalCode());
        appendDetail(sb, "Acquirer Country", metadata.getAcquirerCountry());
        appendDetail(sb, "Invoice Date", String.valueOf( metadata.getInvoiceDate() ) );
        appendDetail(sb, "Invoice Number", metadata.getInvoiceNumber());
        appendDetail(sb, "Address", metadata.getAddress());
        appendDetail(sb, "Document Paid At", metadata.getDocumentPaidAt());
        appendDetail(sb, "Client", metadata.getClient());
        appendDetail(sb, "Currency", metadata.getCurrency());
        appendDetail(sb, "Due Date", metadata.getDueDate());
        appendDetail(sb, "Value Added Tax", metadata.getValueAddedTax());
        appendDetail(sb, "Subtotal", metadata.getSubtotal());
        appendDetail(sb, "Total", metadata.getTotal());
        appendDetail(sb, "Payment Status", metadata.getPaymentStatus());
        appendDetail(sb, "ATCUD", metadata.getAtcud());
        return sb.toString();
    }

    private void appendDetail(StringBuilder sb, String label, String value) {
        if (value != null && !value.isEmpty()) {
            sb.append(label).append(": ").append(value).append("\n");
        }
    }

    public List<AuditLog> downloadLogRepository() {
        logger.info("Downloading all audit logs.");
        return auditLogRepository.findAll();

    }

    public List<AuditLog> getAllAuditLogsWithPaginationSorting(Integer pageNo, Integer pageSize, Sort sort) {
        logger.info("Fetching all audit logs with pagination and sorting - PageNo: {}, PageSize: {}, Sort {}", pageNo, pageSize, sort);
        Pageable paging = PageRequest.of(pageNo, pageSize, sort);
        Page<AuditLog> pageResult = auditLogRepository.findAll( paging );
        if (pageResult.hasContent()) {
            return pageResult.getContent();
        } else {
            return new ArrayList<>();
        }
    }

    public long getAuditLogCount() {
            logger.info("Total number of audit logs: {}", auditLogRepository.count());
            return auditLogRepository.count();

    }

}
