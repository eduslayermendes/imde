package com.example.ImageHandling.domains.dto;

import com.example.ImageHandling.domains.BatchProcess;
import com.example.ImageHandling.domains.InvoiceMetadata;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 10/31/2024
 */
@Data
@RequiredArgsConstructor
public class UploadInvoicesDTO {
	private Map<String,List<InvoiceMetadata>> invoices= new HashMap<>();
	private BatchProcess batchProcess;
	private List<String> errors;


}
