package com.example.ImageHandling.services;

import com.example.ImageHandling.domains.BatchProcess;
import com.example.ImageHandling.domains.BatchProcessFile;
import com.example.ImageHandling.domains.InvoiceMetadata;
import com.example.ImageHandling.domains.Invoices;
import com.example.ImageHandling.domains.types.BatchProcessState;
import com.example.ImageHandling.exception.IllegalDataException;
import com.example.ImageHandling.domains.repository.BatchProcessFilesRepository;
import com.example.ImageHandling.domains.repository.BatchProcessesRepository;
import com.example.ImageHandling.domains.repository.InvoicesRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static java.util.Objects.isNull;

@Slf4j
@Service
public class BatchService {

    private static final Logger logger = LoggerFactory.getLogger(BatchService.class);

    @Autowired
    private BatchProcessesRepository batchProcessesRepository;

    @Autowired
    private BatchProcessFilesRepository batchProcessFilesRepository;

	@Autowired
	private InvoicesRepository invoicesRepository;


    @Transactional
    public BatchProcess createBatchProcess() {
        logger.info("Saving new BatchProcess...");
        BatchProcess batchProcess = new BatchProcess();
        batchProcess.setState( BatchProcessState.UPLOADED.name() );
        batchProcess.setCreatedAt(LocalDateTime.now());
        batchProcess.setUpdatedAt(LocalDateTime.now());
        batchProcess.setExpirationDate(LocalDateTime.now().plusHours(24));
        BatchProcess savedBatchProcess = batchProcessesRepository.save( batchProcess );
        logger.info("BatchProcess created. id: {}", savedBatchProcess.getId());
        return savedBatchProcess;
    }

    @Scheduled(fixedRate = 3600000) // Schedule the service to run every hour to ensure cleanup of DB
    @Transactional
    public void cleanUpExpiredBatchProcesses() {
        logger.info( "Batch cleaning up started." );
        LocalDateTime now = LocalDateTime.now();
        List<BatchProcess> expiredProcesses = batchProcessesRepository.findByExpirationDateBefore(now);

        for (BatchProcess batchProcess : expiredProcesses) {
            deleteBatchProcess(batchProcess.getId());
        }
        logger.info( "Batch cleaning up finished." );
    }

    public void deleteBatchProcess(String batchProcessId) {
        // Find the batch process by id
        BatchProcess batchProcess = batchProcessesRepository.findById(batchProcessId)
                .orElseThrow(() -> new RuntimeException("BatchProcess not found"));

        // Delete all associated batch process files
        List<BatchProcessFile> filesToDelete = batchProcessFilesRepository.findByProcessId(batchProcessId);
        batchProcessFilesRepository.deleteAll(filesToDelete);

        // Then delete the batch process itself
        batchProcessesRepository.delete(batchProcess);
        logger.info("BatchProcess deleted. id: {}", batchProcessId);
    }

    public void deleteBatchProcessFile(String batchProcessFileId) {
        // Find and delete the batch process file by id
        batchProcessFilesRepository.deleteById(batchProcessFileId);
        logger.info("BatchProcessFile deleted. id: {}", batchProcessFileId);
    }



    @Transactional
    public BatchProcess updateBatchProcess(BatchProcess batchProcess) {
        batchProcess.setUpdatedAt(LocalDateTime.now());
        logger.info("BatchProcess updated. id: {}", batchProcess.getId());
        return batchProcessesRepository.save(batchProcess);
    }

    public Optional<BatchProcess> findBatchProcessById(String batchProcessId) {
        logger.info("BatchProcess found. id: {}", batchProcessId);
        return batchProcessesRepository.findById(batchProcessId);
    }

    @Transactional
    public BatchProcessFile createBatchProcessFile(BatchProcessFile batchProcessFile) {
        logger.info("Creating new BatchProcessFile.");
        batchProcessFile.setCreatedAt(LocalDateTime.now());
        batchProcessFile.setUpdatedAt(LocalDateTime.now());
        logger.info("Creating BatchProcessFile for file: {}", batchProcessFile.getFilename());
        return batchProcessFilesRepository.save(batchProcessFile);
    }

    @Transactional
    public BatchProcessFile createBatchProcessFileManual(BatchProcessFile batchProcessFile) {
        logger.info("Creating new BatchProcessFile.");
        batchProcessFile.setCreatedAt(LocalDateTime.now());
        batchProcessFile.setUpdatedAt(LocalDateTime.now());
        logger.info("Creating BatchProcessFile for file: {}", batchProcessFile.getFilename());
        return batchProcessFilesRepository.save(batchProcessFile);
    }

    public Optional<BatchProcessFile> findBatchProcessFileById(String batchProcessFileId) {
        return batchProcessFilesRepository.findById(batchProcessFileId);
    }

    public List<BatchProcessFile> findBatchProcessFileByIds( List<String> batchProcessFileIds) {
        return batchProcessFilesRepository.findAllByProcessIdIn(batchProcessFileIds);
    }

    public List<BatchProcessFile> findBatchProcessFilesByProcessId(String processId) {
        return batchProcessFilesRepository.findByProcessId(processId);
    }

    public List<BatchProcess> findAllBatchProcesses() {
        return batchProcessesRepository.findAll();
    }

    public List<BatchProcess> getAllBatchProcessesWithPaginationSorting(Integer pageNo, Integer pageSize, Sort sort) {
        logger.info("Fetching all BatchProcesses with pagination and sorting - PageNo: {}, PageSize: {}, Sorting: {}",
                pageNo, pageSize, sort);
        Pageable paging = PageRequest.of(pageNo, pageSize, sort);
        Page<BatchProcess> pageResult = batchProcessesRepository.findAll( paging );
        if (pageResult.hasContent()) {
            return pageResult.getContent();
        } else {
            return new ArrayList<>();
        }
    }

    public void deleteBatchProcessFiles(List<String> batchProcessFileIds) {
        logger.info("Deleting BatchProcessFiles by ids: {}", batchProcessFileIds);
        Iterable<BatchProcessFile> iterableFilesToDelete = batchProcessFilesRepository.findAllById(batchProcessFileIds);
        List<BatchProcessFile> filesToDelete = new ArrayList<>();
        iterableFilesToDelete.forEach(filesToDelete::add); // Convert Iterable to List

        // Group files by their process ID
        Map<String, List<BatchProcessFile>> filesByProcessId = new HashMap<>();
        for (BatchProcessFile file : filesToDelete) {
            filesByProcessId.computeIfAbsent(file.getProcessId(), k -> new ArrayList<>()).add(file);
        }


        // Delete the files
        batchProcessFilesRepository.deleteAll(filesToDelete);

        logger.info("Deleting BatchProcessFiles.");

        // Check if any batch process has no remaining files and delete the process if that´s the case
        for (String processId : filesByProcessId.keySet()) {
            List<BatchProcessFile> remainingFiles = findBatchProcessFilesByProcessId(processId);
            if (remainingFiles.isEmpty()) {
                deleteBatchProcess(processId);
            }
        }
    }

    @Transactional
    public BatchProcessFile updateBatchProcessFile(BatchProcessFile batchProcessFile) {
        logger.info("BatchProcessFile updated. id: {}", batchProcessFile.getId());
        return batchProcessFilesRepository.save(batchProcessFile);
    }

    @Transactional
    public void saveBatchFile( String batchProcessId ) {
        log.info("Attempting to save batch process file: {}", batchProcessId);
        if ( isNull( batchProcessId ) || batchProcessId.isEmpty() ) {
            logger.error( "Error on updating batch process file. Batch process id is not provided" );
            throw new IllegalDataException( "Batch process id is empty" );
        }
        Optional<BatchProcess> optionalBatchProcess = findBatchProcessById( batchProcessId );
        if ( optionalBatchProcess.isEmpty() ) {
            logger.error( "Error on updating batch process file. Batch process not found for the id: {}", batchProcessId );
            throw new RuntimeException("Batch process not found");
        }

        BatchProcess batchProcess = optionalBatchProcess.get();
        List<BatchProcessFile> batchProcessFiles = findBatchProcessFilesByProcessId( batchProcessId );
        for ( BatchProcessFile batchProcessFile : batchProcessFiles ) {
            Invoices invoice = Invoices.builder()
                .createdAt( LocalDateTime.now() )
                .invoiceMetadata( batchProcessFile.getMetadata() )
                .fileContent( batchProcessFile.getContent() )
                .fileName( batchProcessFile.getFilename() )
                .layout(batchProcessFile.getLayout() )
                .fileType( batchProcessFile.getFiletype() )
                .build();
            invoicesRepository.save( invoice );
        }
        logger.info("Saving BatchProcessFile and deleting BatchProcess. id: {}", batchProcessId);
        deleteBatchProcess( batchProcessId );
    }

	public List<Map<String, Object>> reviewExtractedData( String batchProcessId ) {
        logger.info( "Start reviewing and extracting data" );
        logger.debug( "Start reviewing and extracting data" );
        if ( isNull( batchProcessId ) || batchProcessId.isEmpty() ) {
            logger.error( "Error on reviewing and extracting data. Batch process id is not provided." );
            throw new IllegalDataException( "Batch process id is empty" );
        }
        List<Map<String, Object>> extractedTexts = new ArrayList<>();
        Optional<BatchProcess> optionalBatchProcess = findBatchProcessById( batchProcessId );
        if ( optionalBatchProcess.isEmpty() ) {
            logger.error( "Error on reviewing and extracting data. Batch process not found for the id: {}", batchProcessId );
            throw new RuntimeException("Batch process not found");
        }
        List<BatchProcessFile> batchProcessFiles = findBatchProcessFilesByProcessId( batchProcessId );
        for ( BatchProcessFile batchProcessFile : batchProcessFiles ) {
            Map<String, Object> fileDetails = new HashMap<>();
            fileDetails.put( "filename", batchProcessFile.getFilename() );
            fileDetails.put( "fileId", batchProcessFile.getId() );
            fileDetails.put( "state", batchProcessFile.getState() );

            InvoiceMetadata metadata = batchProcessFile.getMetadata();
            if ( metadata != null ) {
                fileDetails.put( "metadata", metadata );
            }

            extractedTexts.add( fileDetails );
        }
        logger.debug( "Finish reviewing and extracting data." );
        return extractedTexts;
	}

    public Object getBatchProcessFileDetails( String batchProcessFileId ) {
        logger.info( "Start getting details of process" );
        if ( isNull( batchProcessFileId ) || batchProcessFileId.isEmpty() ) {
            logger.error( "Error on getting details of process. Batch process file id is not provided" );
            throw new IllegalDataException( "Batch process file id is empty" );
        }
        Optional<BatchProcessFile> optionalBatchProcessFile = findBatchProcessFileById( batchProcessFileId );
        if ( optionalBatchProcessFile.isEmpty() ) {
            logger.error( "Error on getting details of process. Batch process not found for the id: {}", batchProcessFileId );
            throw new RuntimeException( "Batch process file not found" );
        }

        BatchProcessFile batchProcessFile = optionalBatchProcessFile.get();
        Map<String, Object> fileDetails = new HashMap<>();
        fileDetails.put( "fileId", batchProcessFile.getId() );
        fileDetails.put( "content", batchProcessFile.getContent() );
        fileDetails.put( "fileType", batchProcessFile.getFiletype() );
        fileDetails.put( "state", batchProcessFile.getState() );
        return fileDetails;
    }
}
