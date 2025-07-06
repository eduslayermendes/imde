package com.example.ImageHandling.domains.dto;

import com.example.ImageHandling.domains.InvoiceMetadata;
import lombok.Builder;
import lombok.Data;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 10/29/2024
 */

@Data
public class ExtractedDataDTO {
	private Boolean isExtractedData;
	private InvoiceMetadata invoiceMetadata;

}
