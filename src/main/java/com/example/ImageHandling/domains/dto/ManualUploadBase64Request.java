package com.example.ImageHandling.domains.dto;

import com.example.ImageHandling.domains.InvoiceMetadata;
import lombok.Data;

@Data
public class ManualUploadBase64Request {
    private String fileBase64;
    private String patternName;
    private String uploadComment;
    private String costCenter;
    private InvoiceMetadata invoiceMetadata;
    private String filetype;
}
