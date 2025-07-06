package com.example.ImageHandling.domains.dto;

import com.example.ImageHandling.domains.BatchProcessFile;
import com.example.ImageHandling.domains.Invoices;
import lombok.Data;

import java.util.List;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 11/18/2024
 */
@Data
public class SaveInvoiceResponseDto {
	private List<Invoices> duplicatedInvoices;
	private List<Invoices> savedInvoices;
	private List<BatchProcessFile> failedBatchProcessFiles;
	private List<BatchProcessFile> savedBatchProcessFiles;
	private List<String> notFoundBatchProcessFileIds;
}