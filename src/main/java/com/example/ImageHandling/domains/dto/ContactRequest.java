package com.example.ImageHandling.domains.dto;

import lombok.Data;

@Data
public class ContactRequest {

    private String email;

    private String name;

    private String requestedBy;
}
