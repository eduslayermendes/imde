package com.example.ImageHandling.controller;

import com.example.ImageHandling.domains.BatchProcess;
import com.example.ImageHandling.domains.BatchProcessFile;
import com.example.ImageHandling.domains.dto.ErrorResponse;
import com.example.ImageHandling.domains.dto.ManualUploadBase64Request;
import com.example.ImageHandling.domains.dto.SaveInvoiceResponseDto;
import com.example.ImageHandling.services.BatchService;
import com.example.ImageHandling.services.InvoiceUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.ImageHandling.utils.ResponseUtil.createErrorResponse;
import static com.example.ImageHandling.utils.SortUtil.getSortingOrders;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/batch")
public class BatchController {

    private static final Logger logger = LoggerFactory.getLogger(BatchController.class);

    private final BatchService batchService;
    private final InvoiceUploadService invoiceUploadService;


    @CrossOrigin(origins = "http://localhost:4200")
    @PostMapping("/manual-upload")
    public ResponseEntity<?> saveBatchFileManually(@RequestBody ManualUploadBase64Request request) {
        try {
            if (request.getFileBase64() == null || request.getFileBase64().isEmpty()) {
                throw new IllegalArgumentException("fileBase64 is null or empty");
            }
            byte[] fileBytes = Base64.getDecoder().decode(request.getFileBase64());

            BatchProcess batchProcess = batchService.createBatchProcess();

            BatchProcessFile batchProcessFile = new BatchProcessFile();
            batchProcessFile.setProcessId(batchProcess.getId());
            batchProcessFile.setFilename(request.getInvoiceMetadata() != null && request.getInvoiceMetadata().getOriginalFileName() != null
                    ? request.getInvoiceMetadata().getOriginalFileName()
                    : "file");
            batchProcessFile.setFiletype(request.getFiletype());
            batchProcessFile.setContent(fileBytes);
            batchProcessFile.setLayout(request.getPatternName());
            batchProcessFile.setState("REVIEW");
            batchProcessFile.setComment(request.getUploadComment());
            batchProcessFile.setCostCenter(request.getCostCenter());
            batchProcessFile.setMetadata(request.getInvoiceMetadata());

            batchService.createBatchProcessFile(batchProcessFile);

            batchProcess.setState("REVIEW");
            batchService.updateBatchProcess(batchProcess);

            Map<String, Object> response = new HashMap<>();
            response.put("batchProcessId", batchProcess.getId());
            response.put("batchProcessFileId", batchProcessFile.getId());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error saving batch file: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving batch file: " + e.getMessage());
        }
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveBatchFile(
            @RequestParam("batchProcessId") String batchProcessId) {
        Map<String, Object> response = new HashMap<>();
            batchService.saveBatchFile( batchProcessId );
            response.put("message", "Invoices saved and batch process data deleted successfully");
            return ResponseEntity.status(HttpStatus.OK).body(response);

    }

    @GetMapping("/batch-processes")
    public ResponseEntity<?> getAllBatchProcesses(
        @RequestParam( defaultValue = "0" ) Integer pageNo,
        @RequestParam( defaultValue = "10" ) Integer pageSize,
        @RequestParam( defaultValue = "createdAt,desc" ) String[] sort
    ) {
            List<BatchProcess> batchProcesses = batchService.getAllBatchProcessesWithPaginationSorting(pageNo, pageSize, Sort.by( getSortingOrders( sort )));
            if (batchProcesses.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(batchProcesses);
            }
            return ResponseEntity.status(HttpStatus.OK).body(batchProcesses);
    }

    @GetMapping("/process/{batchProcessId}")
    public ResponseEntity<?> reviewExtractedData(@PathVariable("batchProcessId") String batchProcessId) {
        Map<String, Object> response = new HashMap<>();
            response.put("batchProcessId", batchProcessId);
            List<Map<String, Object>> extractedTexts = batchService.reviewExtractedData( batchProcessId );
            response.put("extractedTexts", extractedTexts);
            if (extractedTexts.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
            }
            return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/file/{batchProcessFileId}/details")
    public ResponseEntity<?> getBatchProcessFileDetails(@PathVariable("batchProcessFileId") String batchProcessFileId) {
        Map<String, Object> response = new HashMap<>();
            Object fileDetails = batchService.getBatchProcessFileDetails( batchProcessFileId );
            response.put("batchProcessFileDetails", fileDetails);
            return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/delete-files")
    public ResponseEntity<?> deleteBatchProcessFiles(
            @RequestParam("batchProcessFileIds") List<String> batchProcessFileIds) {

        Map<String, Object> response = new HashMap<>();
            batchService.deleteBatchProcessFiles(batchProcessFileIds);
            response.put("message", "Batch process files deleted successfully");
            return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
