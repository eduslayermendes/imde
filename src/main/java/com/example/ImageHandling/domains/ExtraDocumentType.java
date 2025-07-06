package com.example.ImageHandling.domains;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Unwrapped;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 3/12/2025
 */
@Document(collection = "extra_document_types")
@Setter
@Getter
//@EntityListeners( AuditingEntityListener.class)
public class ExtraDocumentType {

	@Id
	private String id;

	@NotBlank(message = "Name cannot be null or empty")
	private String name;

	private String description;

	private Boolean active;

	@CreatedDate
	private LocalDateTime createdDate;

	@CreatedBy
	private String createdBy;


	@LastModifiedDate
	private LocalDateTime lastModifiedDate;

	@LastModifiedBy
	private String lastModifiedBy;

	public ExtraDocumentType() {
	}

	public ExtraDocumentType( String id, String name, String description ) {
		this.id = id;
		this.name = name;
		this.description = description;
	}
}
