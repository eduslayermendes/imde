package com.example.ImageHandling.domains;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 1/31/2025
 */
@Document(collection = "cost_centers")
@Getter
@Setter
public class CostCenter {

	@Id
	private String id;


	@NotBlank(message = "Name cannot be null or empty")
	private String name;

	private Boolean active;

	private String description;

	@CreatedDate
	private LocalDateTime createdDate;

	@CreatedBy
	private String createdBy;


	@LastModifiedDate
	private LocalDateTime lastModifiedDate;

	@LastModifiedBy
	private String lastModifiedBy;



	public CostCenter() {
	}

	public CostCenter( String id, String name, String description ) {
		this.id = id;
		this.name = name;
		this.description = description;
	}
}
