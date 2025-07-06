package com.example.ImageHandling.controller;

import com.example.ImageHandling.domains.dto.EmailRequestDTO;
import com.example.ImageHandling.domains.dto.ErrorResponse;
import com.example.ImageHandling.services.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.example.ImageHandling.utils.ResponseUtil.createErrorResponse;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/email")
public class EmailController {

    private static final Logger logger = LoggerFactory.getLogger(EmailController.class);
    private final EmailService emailService;


    @PostMapping("/sendEmailWithAttachments")
    public ResponseEntity<?> sendEmailWithAttachments(@RequestBody EmailRequestDTO emailRequest) {
            emailService.sendEmailWithAttachments(
                emailRequest.getTo().toArray( new String[0] ),
                emailRequest.getFrom(),
                "invoice@cmas-systems.com",
                emailRequest.getSubject(),
                emailRequest.getText(),
                emailRequest.getInvoiceIds(),
                emailRequest.getLanguage()
            );
            return ResponseEntity.ok("Email sent successfully");

    }

    @PostMapping("/sendSimpleEmail")
    public ResponseEntity<?> sendSimpleEmail(@RequestParam String[] to,
                                @RequestParam String from,
                                @RequestParam String subject,
                                @RequestParam String text) {
            emailService.sendSimpleEmail(to, from, subject, text);
            return ResponseEntity.noContent().build();

    }
}
