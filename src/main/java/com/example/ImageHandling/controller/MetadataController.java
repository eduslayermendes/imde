package com.example.ImageHandling.controller;

import com.example.ImageHandling.domains.InvoiceMetadata;
import com.example.ImageHandling.domains.Invoices;
import com.example.ImageHandling.domains.dto.ErrorResponse;
import com.example.ImageHandling.services.MetadataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.example.ImageHandling.utils.ResponseUtil.createErrorResponse;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/metadata")
public class MetadataController {

    private static final Logger logger = LoggerFactory.getLogger(MetadataController.class);

    private final MetadataService metadataService;

    @PostMapping("/{batchProcessFileId}/edit")
    public ResponseEntity<?> editMetadata(
            @PathVariable("batchProcessFileId") String batchProcessFileId,
            @RequestBody InvoiceMetadata editedMetadata) {
        Map<String, Object> response = new HashMap<>();
            metadataService.editMetadata( batchProcessFileId, editedMetadata );
            response.put( "message", "Metadata updated successfully" );
            return ResponseEntity.status( HttpStatus.OK ).body( response );

    }

    @GetMapping("/file/{batchProcessFileId}")
    public ResponseEntity<?> getBatchProcessFileById(@PathVariable("batchProcessFileId") String batchProcessFileId) {
        Map<String, Object> response = new HashMap<>();
            Map<String, Object> fileDetails = metadataService.getBatchProcessFileById( batchProcessFileId );
            response.put("batchProcessFile", fileDetails);
            return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{id}/edit-invoice-metadata")
    public ResponseEntity<?> editInvoiceMetadata(
            @PathVariable("id") String id,
            @RequestBody InvoiceMetadata updatedMetadata) {

            Optional<Invoices> invoiceOptional = metadataService.editInvoiceMetadata( id, updatedMetadata );
            return invoiceOptional.map( ResponseEntity::ok ).orElseGet( () -> ResponseEntity.notFound().build() );
    }
}
