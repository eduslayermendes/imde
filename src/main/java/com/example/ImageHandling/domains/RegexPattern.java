package com.example.ImageHandling.domains;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "InvoiceLayouts")
@Getter
@Setter
public class RegexPattern {
    @Id
    private String id;

    @NotNull
    private String name;

    private List<FieldMapping> fieldMappings;

    private String language;

    private String dateFormat;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String createdBy;

    public RegexPattern() {}

    public RegexPattern(RegexPattern other) {
        this.id = other.id;
        this.name = other.name;
        this.fieldMappings = other.fieldMappings != null ? new ArrayList<>(other.fieldMappings) : null;
        this.language = other.language;
        this.dateFormat = other.dateFormat;
        this.createdAt = other.createdAt;
        this.updatedAt = other.updatedAt;
        this.createdBy = other.createdBy;
    }

}