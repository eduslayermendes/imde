package com.example.ImageHandling.domains;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Document(collection = "Invoices")
@Builder
@Getter
@Setter
public class Invoices {
    @Id
    private String invoiceid;

    @NotNull
    private String fileName;

    @NotNull
    private String fileType;

    @NotNull
    private byte[] fileContent;

    private String layout;

    private InvoiceMetadata invoiceMetadata;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String createdBy;
}
