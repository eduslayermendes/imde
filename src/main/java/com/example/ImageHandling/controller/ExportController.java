package com.example.ImageHandling.controller;

import com.example.ImageHandling.domains.dto.ErrorResponse;
import com.example.ImageHandling.domains.dto.IdListRequest;
import com.example.ImageHandling.domains.AuditLog;
import com.example.ImageHandling.domains.repository.InvoicesRepository;
import com.example.ImageHandling.services.AuditLogService;
import com.example.ImageHandling.services.ExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.ImageHandling.utils.ResponseUtil.createErrorResponse;
import static com.example.ImageHandling.utils.SortUtil.getSortingOrders;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/export")
public class ExportController {

    private static final Logger logger = LoggerFactory.getLogger(ExportController.class);
    private final InvoicesRepository invoicesRepository;
    private final AuditLogService auditLogService;
    private final ExportService exportService;

    @PostMapping( "/download" )
    public ResponseEntity<?> downloadSelectedFiles( @RequestBody IdListRequest idListRequest, @RequestParam String language ) {
            byte[] zipBytes = exportService.downloadSelectedFiles( idListRequest, language );
            HttpHeaders headers = exportService.createHeaders( zipBytes.length, idListRequest.getIds().stream()
                .map( id -> invoicesRepository.findById( id ).orElse( null ) )
                .collect( Collectors.toList() ) );
            return new ResponseEntity<>( zipBytes, headers, HttpStatus.OK );

    }

    @PreAuthorize("hasAnyAuthority(@logsAllowedRoles)")
    @GetMapping( "/logs" )
    public ResponseEntity<?> getDownloadLogs(
        @RequestParam( defaultValue = "0" ) Integer pageNo,
        @RequestParam( defaultValue = "10" ) Integer pageSize,
        @RequestParam( defaultValue = "timestamp,desc" ) String[] sort
    ) {
            List<AuditLog> logs = auditLogService.getAllAuditLogsWithPaginationSorting( pageNo, pageSize, Sort.by( getSortingOrders( sort ) ) );
            return ResponseEntity.ok( logs );


    }

    @PreAuthorize("hasAnyAuthority(@logsAllowedRoles)")
    @GetMapping("/count")
    public ResponseEntity<?> count() {
            return ResponseEntity.ok( auditLogService.getAuditLogCount() ) ;

    }

}
