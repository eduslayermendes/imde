package com.example.ImageHandling.domains;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Document(collection = "batch_process_files")
public class BatchProcessFile {

    @Id
    private String id;

    @NotNull
    private String processId;

    @NotNull
    private String filename;

    private String filetype;

    private byte[] content;

    private String layout;

    private String state;

    private LocalDateTime createdAt;

    private LocalDateTime  updatedAt;

    private InvoiceMetadata metadata;

    private String createdBy;

    @Transient
    private String comment;

    private String costCenter;
}
