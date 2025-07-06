package com.example.ImageHandling.domains;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "Issuers")
@Getter
@Setter
public class Issuer {
    @Id
    private String id;

    @NotNull
    private String name;

    @JsonProperty("isPt")
    private boolean isPt;
    private List<String> layoutIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;

    public boolean isPt() {
        return isPt;
    }

    public void setPt(boolean pt) {
        isPt = pt;
    }

    public Issuer() {}

    public Issuer(Issuer other) {
        this.id = other.id;
        this.name = other.name;
        this.isPt = other.isPt;
        this.layoutIds = other.layoutIds;
        this.createdAt = other.createdAt;
        this.updatedAt = other.updatedAt;
        this.createdBy = other.createdBy;
    }

}
