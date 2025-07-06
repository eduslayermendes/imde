package com.example.ImageHandling.services;

import com.example.ImageHandling.domains.Invoices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TableService {

	private final InvoiceService invoiceService;
	private final Logger logger = LoggerFactory.getLogger( TableService.class );

	public List<Invoices> getAllInvoicesWithPaginationFilters(
		Integer pageNo, Integer pageSize, Sort sort, String searchFileName, String issuer, String createdBy, String createdAtStartDate,  String createdAtEndDate, String invoiceStartDate, String invoiceEndDate, String invoiceNumber, String searchCostCenter, String[] costCenters
	) {
		logger.info("Attempting to get invoices with pagination sorting and search");
		return invoiceService.getAllInvoicesWithPaginationSortingAndSearch(pageNo, pageSize, sort, searchFileName, issuer, createdBy, createdAtStartDate, createdAtEndDate, invoiceStartDate, invoiceEndDate, invoiceNumber, searchCostCenter, costCenters);
	}


	public void deleteInvoice( String id ) {
		logger.info("Attempting to delete invoice with id: {}", id);
		invoiceService.logDeleteAction( id );
		invoiceService.deleteInvoice( id );
	}
}
