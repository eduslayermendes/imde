package com.example.ImageHandling.domains;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
public class InvoiceMetadata {

	private String issuerVATNumber;

	private String acquirerVATNumber;

	private String companyName;

	private String site;

	private String phoneNumber;

	private String email;

	private String postalCode;

	private String acquirerCountry;

	private LocalDate invoiceDate;

	private String invoiceNumber;

	private String address;

	private List<Item> items;

	private String documentPaidAt;

	private String client;

	private String currency;

	private String dueDate;

	private String valueAddedTax;

	private String subtotal;

	private String total;

	private String paymentStatus;

	private String atcud;

	private String originalFileName;

	private String comment;

	private String costCenter;

	public boolean isEmpty() {
		return this.equals( new InvoiceMetadata() );
	}

}
