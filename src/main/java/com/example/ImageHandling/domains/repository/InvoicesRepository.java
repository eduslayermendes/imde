// ExtractedTextRepository.java
package com.example.ImageHandling.domains.repository;

import com.example.ImageHandling.domains.Invoices;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InvoicesRepository extends MongoRepository<Invoices, String> {

	List<Invoices> findTop20ByOrderByCreatedAtDesc();
	Page<Invoices> findAll( Pageable pageable );

	/**
	 * Finds possible duplicate invoices by issuer VAT number, invoice date, and invoice number.
	 *
	 * @param issuerVATNumber The VAT number of the issuer.
	 * @param invoiceDate The date of the invoice.
	 * @param invoiceNumber The invoice number.
	 * @return List of invoices matching the criteria.
	 */
	@Query("{ 'invoiceMetadata.issuerVATNumber': ?0, 'invoiceMetadata.invoiceDate': ?1, 'invoiceMetadata.invoiceNumber': ?2 }")
	List<Invoices> findDuplicateInvoices(String issuerVATNumber, LocalDate invoiceDate, String invoiceNumber);

	/**
	 * Finds all invoices with the given issuer VAT number and a non-empty company name.
	 *
	 * @param issuerVATNumber The VAT number of the issuer.
	 * @return List of invoices with a non-empty company name for the given VAT number.
	 */
	@Query("{ 'invoiceMetadata.issuerVATNumber': ?0, 'invoiceMetadata.companyName': { $ne: '' } }")
	List<Invoices> findByIssuerVATNumberAndCompanyNameNotEmpty(String issuerVATNumber);

}
