package com.example.ImageHandling.services;

import com.example.ImageHandling.domains.AuditLog;
import com.example.ImageHandling.domains.InvoiceMetadata;
import com.example.ImageHandling.domains.Invoices;
import com.example.ImageHandling.domains.types.LogOperation;
import com.example.ImageHandling.exception.IllegalDataException;
import com.example.ImageHandling.domains.repository.AuditLogRepository;
import com.example.ImageHandling.domains.repository.InvoicesRepository;
import com.example.ImageHandling.utils.ExcelUtils;
import com.example.ImageHandling.utils.MailUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import static java.util.Objects.isNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final MailUtils mailUtils;
    private final InvoicesRepository invoicesRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuthService authService;



    public void sendSimpleEmail(String[] to, String from, String subject, String text) {
        logger.info("Attempting to send simple email from {} to {}", from, to);
        mailUtils.sendEmail(from, to, subject, text);
    }

    public void sendEmailWithAttachments(String[] to, String from, String cc, String subject, String text, List<String> invoiceIds, String language) {
        logger.info("Attempting to send email from {} to {}", from, to);
        if ( isNull( to ) || to.length == 0 ) {
            logger.error( "Email must have at least one recipient." );
            throw new IllegalDataException( "Email must have at least one recipient." );
        }
        try {
            List<Invoices> invoices = invoiceIds.stream()
                    .map(id -> invoicesRepository.findById(id).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());


            LocalDateTime firstCreatedAt = invoices.stream()
                    .filter(Objects::nonNull)
                    .map(Invoices::getCreatedAt)
                    .min(LocalDateTime::compareTo)
                    .orElse(null);
            LocalDateTime lastCreatedAt = invoices.stream()
                    .filter(Objects::nonNull)
                    .map(Invoices::getCreatedAt)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);

            String filename = ExportService.createExportFileName(firstCreatedAt, lastCreatedAt);

            byte[] zipData;
            try {
                zipData = ExcelUtils.generateExcelFile(invoices, language, filename);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }


            List<MailUtils.Attachment> attachments = List.of(new MailUtils.Attachment(filename, zipData));
            boolean success = mailUtils.sendEmailWithAttachments(from, to, cc, subject, text, attachments).join();

            logger.info("Email sent successfully from {} to {}", from, to);

            if (success) {
                List<String> fileNames = invoices.stream()
                        .map(Invoices::getFileName)
                        .collect(Collectors.toList());

                List<InvoiceMetadata> metadataList = invoices.stream()
                        .map(Invoices::getInvoiceMetadata)
                        .collect(Collectors.toList());

                AuditLog log = new AuditLog();
                log.setUsername( authService.getLoggedInUserDetails().getUsername() );
                log.setOperation( LogOperation.SEND_EMAIL.toString() );
                log.setFileName(filename);
                log.setTimestamp(LocalDateTime.now());
                log.setExportedFileNames(fileNames);
                log.setExportedMetadata(metadataList);
                auditLogRepository.save(log);
            }
        }
        catch ( Exception e ) {
            String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
            logger.error( "Error on sending email. {}", rootCauseMessage, e );
            throw new RuntimeException( String.format( "Error on sending email. %s", rootCauseMessage ) );
        }
    }
}
