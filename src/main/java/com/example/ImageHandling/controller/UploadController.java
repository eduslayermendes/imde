package com.example.ImageHandling.controller;

import com.example.ImageHandling.domains.CostCenter;
import com.example.ImageHandling.domains.dto.ErrorResponse;
import com.example.ImageHandling.domains.dto.UploadInvoicesDTO;
import com.example.ImageHandling.domains.dto.UploadObjectRecord;
import com.example.ImageHandling.services.UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static com.example.ImageHandling.utils.ResponseUtil.createErrorResponse;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/upload")
public class UploadController {


    private final UploadService uploadService;

    @PostMapping
    public ResponseEntity<?> uploadAndExtract(
            @RequestParam("file") MultipartFile file,
            @RequestParam("patternName") String patternName,
            @RequestParam("uploadComment") String uploadComment,
            @RequestParam("costCenter") String costCenter) throws Exception {

        List<String> errors = new ArrayList<>();
        Optional<UploadInvoicesDTO> results = uploadService.uploadAndExtract(
                file, patternName, uploadComment, costCenter, errors);

        Map<String, Object> response = new HashMap<>();
        response.put("results", results);
        if (!errors.isEmpty()) {
            response.put("errors", errors);
        }

        return ResponseEntity.ok(response);
    }

    private static ResponseEntity<Map<String, Object>> populateResponse( Optional<UploadInvoicesDTO> result, Map<String, Object> response, List<String> errors ) {
        UploadInvoicesDTO uploadInvoicesDTO = result.get();

        if ( uploadInvoicesDTO.getInvoices() != null && uploadInvoicesDTO.getInvoices().containsKey( "uploadedInvoices" ) ) {
            response.put( "uploadedInvoices", uploadInvoicesDTO.getInvoices().get( "uploadedInvoices" ) );
        }

        if ( uploadInvoicesDTO.getBatchProcess() != null ) {
            response.put( "batchProcessId", uploadInvoicesDTO.getBatchProcess().getId() );
        }

        if ( !errors.isEmpty() ) {
            response.put( "errors", errors );
            response.put( "warning", "Some files failed to process." );
        }

        if ( uploadInvoicesDTO.getInvoices() != null && uploadInvoicesDTO.getInvoices().containsKey( "duplicatedInvoices" ) ) {
            response.put( "duplicatedInvoices", uploadInvoicesDTO.getInvoices().get( "duplicatedInvoices" ) );
            return ResponseEntity.status( HttpStatus.MULTI_STATUS ).body( response );
        }
        return ResponseEntity.status( HttpStatus.CREATED ).body( response );
    }

}
