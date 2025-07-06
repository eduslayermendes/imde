package com.example.ImageHandling.domains;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 2/21/2025
 */
@Document(collection = "extra_documents")
@Data
public class ExtraDocument {
	@Id
	private String id;

	@NotNull
	private String type;

	private String name;

	@NotNull
	private String fileName;

	private String month;

	@CreatedDate
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime createdAt;

	@LastModifiedDate
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime updatedAt;

	@CreatedBy
	private String createdBy;

	private String description;

	@NotNull
	@JsonIgnore
	private byte[] file;

	public ExtraDocument( String type, String name, String fileName, String month, String description, @NotNull byte[] file ) {
		this.type = type;
		this.name = name;
		this.fileName = fileName;
		this.month = month;
		this.description = description;
		this.file = file;
	}
}
