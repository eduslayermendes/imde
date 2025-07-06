package com.example.ImageHandling.domains;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Builder
@EnableMongoAuditing
@Data
@Document(collection = "contacts")
public class Contact {
    @Id
    private String id;

    private String email;

    @NotNull
    private String name;

    @LastModifiedDate
    private Date updatedAt;

    private String requestedBy;
}
