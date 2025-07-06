package com.example.ImageHandling.domains;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;


@Data
@Document(collection = "batch_processes")
public class BatchProcess {

    @Id
    private String id;

    @NotNull
    private String state;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime expirationDate;

}