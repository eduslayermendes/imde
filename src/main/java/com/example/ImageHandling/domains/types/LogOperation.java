package com.example.ImageHandling.domains.types;

public enum LogOperation {
	SEND_EMAIL( "Send Email" ),
	EXPORT( "Export" ),
	DELETE( "Delete" ),
	ISSUER_UPDATED( "Issuer Updated" ),
	ISSUER_CREATED( "Issuer Created" ),
	ISSUER_DELETED(	"Issuer Deleted" ),
	ISSUER_DELETE_FAILED( "Issuer Delete Failed" ),
	INVOICE_UPDATED ("Invoice Updated"),
	PATTERN_CREATED ("Pattern Created"),
	EXTRA_DOCUMENT_UPLOADED("Extra Document Uploaded"),
	EXTRA_DOCUMENT_DELETED("Extra Document Deleted"),
	EXTRA_DOCUMENT_EXPORTED("Extra Document Exported"),
	COST_CENTER_CREATED ("Cost Center Created"),
	COST_CENTER_UPDATED ("Cost Center Updated"),
	EXTRA_DOCUMENT_TYPE_CREATED("Extra Document Type Created"),
	EXTRA_DOCUMENT_TYPE_DELETED("Extra Document Type Deleted"),
	EXTRA_DOCUMENT_TYPE_UPDATED("Extra Document Type Updated"),

	;

	private final String val;

	private LogOperation( String val ) {
		this.val = val;
	}

	@Override
	public String toString() {
		return val;
	}
}
