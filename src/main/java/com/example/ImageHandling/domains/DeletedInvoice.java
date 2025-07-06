package com.example.ImageHandling.domains;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "DeletedInvoices")
@Getter
@Setter
public class DeletedInvoice {

    @Id
    private String invoiceid;

    private String fileName;

    private String fileType;

    private byte[] fileContent;

    private String layout;

    private InvoiceMetadata invoiceMetadata;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String createdBy;
 
}
