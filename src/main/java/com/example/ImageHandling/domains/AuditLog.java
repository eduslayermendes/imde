// AuditLog.java
package com.example.ImageHandling.domains;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "Logs")
@Getter
@Setter
public class AuditLog {
    @Id
    private String id;
    @NotNull
    private String operation;
    @NotNull
    private String username;
    private String fileName;
    private String fileType;
    private String fileContent;
    private String oldState;
    private String newState;
    private String layout;
    private String invoiceMetadata;
    private LocalDateTime timestamp;
    private List<String> exportedFiles;
    private List<String> exportedFileNames;
    private List<InvoiceMetadata> exportedMetadata;

    // getters and setters
}
