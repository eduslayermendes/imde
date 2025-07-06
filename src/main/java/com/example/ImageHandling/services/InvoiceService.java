package com.example.ImageHandling.services;

import com.example.ImageHandling.domains.*;
import com.example.ImageHandling.domains.dto.SaveInvoiceResponseDto;
import com.example.ImageHandling.domains.types.LogOperation;
import com.example.ImageHandling.exception.IllegalDataException;
import com.example.ImageHandling.domains.repository.AuditLogRepository;
import com.example.ImageHandling.domains.repository.DeletedInvoiceRepository;
import com.example.ImageHandling.domains.repository.InvoicesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.Objects.isNull;

/**
 * Service class for handling Invoices.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceService {

    public static final Logger logger = LoggerFactory.getLogger(InvoiceService.class);
    private final InvoicesRepository invoicesRepository;
    private final AuditLogRepository auditLogRepository;
    private final BatchService batchService;
    private final AuditLogService auditLogService;
    private final DeletedInvoiceRepository deletedInvoiceRepository;
    private final MongoTemplate mongoTemplate;
    private final AuthService authService;


    /**
     * Retrieves an invoice by its ID.
     *
     * @param id The ID of the invoice to retrieve.
     * @return The found Invoices object, or null if not found.
     */
    public Invoices getInvoice(String id) {
        logger.info("Fetching invoice with ID: {}", id);
        return invoicesRepository.findById(id).orElse(null);  // Find invoice by ID in repository
    }

    /**
     * Retrieves all invoices stored in the database.
     *
     * @return List of all Invoices objects stored in the database.
     */
    public List<Invoices> getAllInvoices() {
        logger.info("Fetching all invoices from the database");
        return invoicesRepository.findAll();
    }

    public List<Invoices> getAllInvoicesWithPaginationSorting(Integer pageNo, Integer pageSize, Sort sort) {
        logger.info("Fetching invoices with pagination - PageNo: {}, PageSize: {}, Sort: {}", pageNo, pageSize, sort);
        Pageable paging = PageRequest.of(pageNo, pageSize, sort);
        Page<Invoices> pageResult = invoicesRepository.findAll( paging );
        if (pageResult.hasContent()) {
            return pageResult.getContent();
        } else {
            return new ArrayList<>();
        }
    }

    public List<Invoices> getAllInvoicesWithPaginationSortingAndSearch(
        Integer pageNo, Integer pageSize, Sort sort, String searchFileName, String issuer, String createdBy, String createdAtStartDate, String createdAtEndDate, String invoiceStartDate,
        String invoiceEndDate, String invoiceNumber, String searchCostCenter, String[] userDedicatedCostCenters
    ) {
        if (isSearchCostCenterInvalid(searchCostCenter, userDedicatedCostCenters)) {
            return new ArrayList<>();
        }

        logger.info(
            "Fetching invoices with pagination and search - PageNo: {}, PageSize: {}, Sort:{}, SearchFileName: {}, Issuer: {}, CreatedBy: {}, InvoiceStartDate: {}, InvoiceEndDate: {}, StartDate: {}, EndDate: {}, InvoiceNumber: {}, CostCenter: {}, , User Dedicated Cost Centers: {} ",
            pageNo, pageSize, sort, searchFileName, issuer, createdBy, invoiceStartDate, invoiceEndDate, createdAtStartDate, createdAtEndDate, invoiceNumber, searchCostCenter,
            Arrays.toString( userDedicatedCostCenters ) );
        if ( userDedicatedCostCenters == null || userDedicatedCostCenters.length == 0 ) {
            logger.warn( "User does not have dedicated cost centers. Returning empty result." );
            return new ArrayList<>();
        }
        Pageable pageable = PageRequest.of( pageNo, pageSize, sort );
        Query query = new Query().with( pageable );
        prepareQuery( searchFileName, issuer, createdBy, createdAtStartDate, createdAtEndDate, invoiceStartDate, invoiceEndDate, invoiceNumber, searchCostCenter, userDedicatedCostCenters, query );
        return mongoTemplate.find( query, Invoices.class );
    }

    public long getFilteredInvoiceCount( String searchFileName, String issuer, String createdBy, String createdAtStartDate, String createdAtEndDate, String invoiceStartDate, String invoiceEndDate,
        String invoiceNumber, String searchCostCenter, String[] userDedicatedCostCenters ) {
        logger.info( "Attempting to get filtered invoice count" );
        if (isSearchCostCenterInvalid(searchCostCenter, userDedicatedCostCenters)) {
            return 0;
        }
        logger.info(
            "Counting filtered invoices - SearchFileName: {}, Issuer: {}, CreatedBy: {}, InvoiceStartDate: {}, InvoiceEndDate: {}, StartDate: {}, EndDate: {}, InvoiceNumber: {}, , CostCenter: {}, User Dedicated Cost Centers: {} ",
            searchFileName, issuer, createdBy, invoiceStartDate, invoiceEndDate, createdAtStartDate, createdAtEndDate, invoiceNumber, searchCostCenter,
            Arrays.toString( userDedicatedCostCenters ) );
        if ( userDedicatedCostCenters == null || userDedicatedCostCenters.length == 0 ) {
            logger.warn( "User does not have dedicated cost centers. Returning empty result." );
            return 0;
        }
        Query query = new Query();
        prepareQuery( searchFileName, issuer, createdBy, createdAtStartDate, createdAtEndDate, invoiceStartDate, invoiceEndDate, invoiceNumber, searchCostCenter, userDedicatedCostCenters, query );
        logger.info( "Filtered invoices count: {}", mongoTemplate.count( query, Invoices.class ) );
        return mongoTemplate.count( query, Invoices.class );
    }



    /**
     * Retrieves invoices by their IDs.
     *
     * @param ids List of IDs of the invoices to retrieve.
     * @return List of found Invoices objects.
     */
    public List<Invoices> findInvoicesByIds(List<String> ids) {
        logger.info("Fetching invoices with IDs: {}", ids);
        Iterable<Invoices> iterable = invoicesRepository.findAllById(ids);
        return StreamSupport.stream(iterable.spliterator(), false)
                .collect(Collectors.toList());
    }

    /**
     * Deletes an invoice by its ID.
     *
     * @param id The ID of the invoice to delete.
     */
    public void deleteInvoice(String id) {
        logger.info("Attempting to delete invoice with ID: {}", id);
        Invoices invoice = invoicesRepository.findById(id).orElse(null);
        if (invoice != null) {
            DeletedInvoice deletedInvoice = new DeletedInvoice();
            deletedInvoice.setInvoiceid(invoice.getInvoiceid());
            deletedInvoice.setFileName(invoice.getFileName());
            deletedInvoice.setFileType(invoice.getFileType());
            deletedInvoice.setFileContent(invoice.getFileContent());
            deletedInvoice.setLayout(invoice.getLayout());
            deletedInvoice.setInvoiceMetadata(invoice.getInvoiceMetadata());
            deletedInvoice.setCreatedAt(invoice.getCreatedAt());
            deletedInvoice.setUpdatedAt(invoice.getUpdatedAt());
            deletedInvoice.setCreatedBy(invoice.getCreatedBy());

            deletedInvoiceRepository.save(deletedInvoice);

            invoicesRepository.deleteById(id);
            logger.info("Invoice '{}' ({}) deleted successfully", invoice.getFileName(), id);
        }
        else {
            logger.warn("Invoice with ID '{}' not found for deletion", id);
        }
    }

    public void logDeleteAction(String id) {
        Invoices invoice = invoicesRepository.findById(id).orElse(null);
        if (invoice != null) {
            AuditLog log = new AuditLog();
            log.setOperation( LogOperation.DELETE.toString() );
            log.setFileName(invoice.getFileName());
            log.setUsername( authService.getLoggedInUserDetails().getUsername() );
            log.setTimestamp(LocalDateTime.now());
            auditLogRepository.save(log);
        } else {
            logger.warn( "Invoice ID {} not found.", id);
        }
    }

    public long getInvoiceCount() {
        logger.info("Fetching total count of invoices");
        return invoicesRepository.count();
    }

    public List<Invoices> findTop20ByOrderByCreatedAtDesc() {
        logger.info("Fetching top 20 most recent invoices");
        return invoicesRepository.findTop20ByOrderByCreatedAtDesc();
    }

///////Mine Implementation//////////////////////////////////////////
    public SaveInvoiceResponseDto saveManuallyAddedData(List<BatchProcessFile> batchProcessFileIds) {
        logger.info("Saving manually added invoice data");

        if (isNull(batchProcessFileIds) || batchProcessFileIds.isEmpty()) {
            throw new IllegalDataException("Batch process file Id is empty");
        }

        SaveInvoiceResponseDto result = new SaveInvoiceResponseDto();
        List<String> notFoundBatchProcessFileIds = new ArrayList<>();
        List<Invoices> duplicateInvoices = new ArrayList<>();
        List<Invoices> savedInvoices = new ArrayList<>();
        List<BatchProcessFile> batchProcessFileWithOutDuplicateInvoice = new ArrayList<>();
        List<BatchProcessFile> batchProcessFileWithDuplicateInvoice = new ArrayList<>();

        try {
            logger.debug("Start saving manually added invoice data");

            for (BatchProcessFile batchProcessFileId : batchProcessFileIds) {
                BatchProcessFile createdFile = batchService.createBatchProcessFile(batchProcessFileId);

                if (createdFile == null) {
                    logger.error("Batch process file not found for ID: {}", batchProcessFileId.getId());
                    notFoundBatchProcessFileIds.add(String.valueOf(batchProcessFileId.getId()));
                    continue;
                }

                Optional<List<Invoices>> duplicateInvoicesList = findDuplicateInvoices(
                        createdFile.getMetadata().getIssuerVATNumber(),
                        createdFile.getMetadata().getInvoiceDate(),
                        createdFile.getMetadata().getInvoiceNumber()
                );

                if (duplicateInvoicesList.isPresent() && !duplicateInvoicesList.get().isEmpty()) {
                    logger.error("Duplicate invoices found. No save operation performed: {}", duplicateInvoicesList.get());
                    duplicateInvoices.addAll(duplicateInvoicesList.get());
                    batchProcessFileWithDuplicateInvoice.add(createdFile);
                    batchService.deleteBatchProcessFile(createdFile.getId());
                } else {
                    // Save new invoice
                    Invoices invoice = Invoices.builder()
                            .createdAt(LocalDateTime.now())
                            .invoiceMetadata(createdFile.getMetadata())
                            .fileContent(createdFile.getContent())
                            .fileName(createdFile.getFilename())
                            .fileType(createdFile.getFiletype())
                            .layout("Manual")
                            .createdBy(authService.getLoggedInUserDetails().getUsername())
                            .build();

                    Invoices savedInvoice = invoicesRepository.save(invoice);
                    savedInvoices.add(savedInvoice);
                    batchProcessFileWithOutDuplicateInvoice.add(createdFile);

                    auditLogService.logSubmit(authService.getLoggedInUserDetails().getUsername(), createdFile.getFilename());
                    batchService.deleteBatchProcessFile(createdFile.getId());
                }
            }

            if (!batchProcessFileWithDuplicateInvoice.isEmpty()) {
                result.setFailedBatchProcessFiles(batchProcessFileWithDuplicateInvoice);
            }

            if (!batchProcessFileWithOutDuplicateInvoice.isEmpty()) {
                result.setSavedBatchProcessFiles(batchProcessFileWithOutDuplicateInvoice);
            }

            if (!notFoundBatchProcessFileIds.isEmpty()) {
                result.setNotFoundBatchProcessFileIds(notFoundBatchProcessFileIds);
            }

            if (!duplicateInvoices.isEmpty()) {
                result.setDuplicatedInvoices(duplicateInvoices);
            }

            if (!savedInvoices.isEmpty()) {
                result.setSavedInvoices(savedInvoices);
            }

            logger.debug("Saving manually added invoices data finished.");
            return result;

        } catch (Exception e) {
            String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
            logger.error("Error on saving extracted data. {}", rootCauseMessage, e);
            throw new RuntimeException(String.format("Error on saving extracted data. %s", rootCauseMessage));
        }
    }
    ////////////////////////////////////////////////////////////

    // Saving invoice in the invoice collection
    public SaveInvoiceResponseDto saveExtractedData( List<String> batchProcessFileIds ) {
        logger.info( "Saving extracted invoice data" );
        SaveInvoiceResponseDto result = new SaveInvoiceResponseDto();
        List<String> notFoundBatchProcessFileIds = new ArrayList<>();

        if ( isNull( batchProcessFileIds ) || batchProcessFileIds.isEmpty()) {
            throw new IllegalDataException( "Batch process file Id is empty" );
        }
        try {
            logger.debug( "Start saving extracted invoice data" );

            List<Invoices> duplicateInvoices = new ArrayList<>();
            List<Invoices> savedInvoices = new ArrayList<>();
            List<BatchProcessFile> batchProcessFileWithOutDuplicateInvoice  = new ArrayList<>();
            List<BatchProcessFile> batchProcessFileWithDuplicateInvoice = new ArrayList<>();

            for ( String batchProcessFileId : batchProcessFileIds ){
                Optional<BatchProcessFile> foundBatchProcessFile = batchService.findBatchProcessFileById( batchProcessFileId );

                if ( foundBatchProcessFile.isPresent() ){
                    BatchProcessFile batchProcessFile = foundBatchProcessFile.get();
                    //Find duplicate invoices
                    Optional<List<Invoices>> duplicateInvoicesList = findDuplicateInvoices( batchProcessFile.getMetadata().getIssuerVATNumber(), batchProcessFile.getMetadata().getInvoiceDate(), batchProcessFile.getMetadata().getInvoiceNumber() );
                    if ( duplicateInvoicesList.isPresent() && !duplicateInvoicesList.get().isEmpty() ) {
                        logger.error( "Duplicate invoices found. No save operation performed. {}", duplicateInvoicesList.get() );
                        duplicateInvoices.addAll( duplicateInvoicesList.get() );
                        batchProcessFileWithDuplicateInvoice.add(batchProcessFile);
                        batchService.deleteBatchProcessFile( batchProcessFile.getId() );
                    } else{
                        batchProcessFileWithOutDuplicateInvoice.add(batchProcessFile);
                        /////////////////////////////// Starting saving invoices //////////////////////////////////////

                            Invoices invoice = Invoices.builder()
                                .createdAt( LocalDateTime.now() )
                                .invoiceMetadata( batchProcessFile.getMetadata() )
                                .fileContent( batchProcessFile.getContent() )
                                .fileName( batchProcessFile.getFilename() )
                                .fileType( batchProcessFile.getFiletype() )
                                .layout(batchProcessFile.getLayout())
                                .createdBy( authService.getLoggedInUserDetails().getUsername() )
                                .build();
                            Invoices savedInvoice = invoicesRepository.save( invoice );
                            savedInvoices.add( savedInvoice );

                            auditLogService.logSubmit( authService.getLoggedInUserDetails().getUsername(), batchProcessFile.getFilename() );

                        batchService.deleteBatchProcessFile( batchProcessFile.getId() );
                        /////////////////////////////// Finishing saving invoices //////////////////////////////////////
                    }
                }
                else {
                    logger.error( "Batch process file not found" );
                    notFoundBatchProcessFileIds.add( batchProcessFileId );
                }

            }

            if ( !batchProcessFileWithDuplicateInvoice.isEmpty() ){
                result.setFailedBatchProcessFiles( batchProcessFileWithDuplicateInvoice );
            }
            if ( !batchProcessFileWithOutDuplicateInvoice.isEmpty() ){
                result.setSavedBatchProcessFiles( batchProcessFileWithOutDuplicateInvoice );
            }
            if ( !notFoundBatchProcessFileIds.isEmpty() ){
                result.setNotFoundBatchProcessFileIds( notFoundBatchProcessFileIds );
            }

            if ( !duplicateInvoices.isEmpty() ){
                result.setDuplicatedInvoices( duplicateInvoices );
            }

            if ( !savedInvoices.isEmpty() ){
                result.setSavedInvoices( savedInvoices );
            }

            logger.debug( "Saving extracted invoices data finished." );
            // Always return result, even if only duplicates (no saved invoices)
            return result;

        }
        catch ( Exception e ) {
            String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
            logger.error( "Error on saving extracted data. {}", rootCauseMessage, e );
            throw new RuntimeException( String.format( "Error on saving extracted data. %s", rootCauseMessage ) );
        }
    }

    private void prepareQuery( String searchFileName, String issuer, String createdBy,
        String createdAtStartDate, String createdAtEndDate,
        String invoiceStartDate, String invoiceEndDate, String invoiceNumber,String searchCostCenter, String[] costCenters, Query query ) {

        if (isNotBlank( searchFileName )) {
            query.addCriteria(Criteria.where("fileName").regex( searchFileName, "i"));
        }
        if (isNotBlank( issuer )) {
            query.addCriteria(Criteria.where("invoiceMetadata.companyName").regex( issuer, "i"));
        }
        if (isNotBlank( createdBy )) {
            query.addCriteria(Criteria.where("createdBy").regex( createdBy, "i"));
        }
        if (isNotBlank( invoiceNumber )) {
            query.addCriteria(Criteria.where("invoiceMetadata.invoiceNumber").regex( invoiceNumber));
        }

        if (isNotBlank( createdAtStartDate ) && isNotBlank( createdAtEndDate )) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            Date startDate = Date.from(LocalDate.parse(createdAtStartDate, formatter).atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(LocalDate.parse(createdAtEndDate, formatter).atTime( LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());
            query.addCriteria(Criteria.where("createdAt").gte(startDate).lte(endDate));
        }

        if (isNotBlank( invoiceStartDate ) && isNotBlank( invoiceEndDate )) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            Date startDate = Date.from(LocalDate.parse( invoiceStartDate, formatter).atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(LocalDate.parse( invoiceEndDate, formatter).atTime( LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());
            query.addCriteria( Criteria.where("invoiceMetadata.invoiceDate").gte(startDate).lte(endDate));
        }

        if ( searchCostCenter != null && !searchCostCenter.isEmpty() && isUserAuthorized( searchCostCenter, costCenters ) ) {
            query.addCriteria(Criteria.where("invoiceMetadata.costCenter").regex( searchCostCenter, "i"));
        }
        if ( costCenters != null && costCenters.length > 0 && ( searchCostCenter == null || Objects.requireNonNull( searchCostCenter ).isEmpty() )  ) {
            List<Criteria> costCenterCriteria = new ArrayList<>();
            for (String center : costCenters) {
                costCenterCriteria.add(Criteria.where("invoiceMetadata.costCenter").regex("^" + center + "$", "i"));
            }
            query.addCriteria(new Criteria().orOperator(costCenterCriteria.toArray(new Criteria[0])));
        }

    }



    public Optional<List<Invoices>> findDuplicateInvoices(String issuerVATNumber, LocalDate invoiceDate, String invoiceNumber){
        log.info( "Finding duplicate invoices by Issuer VAT Number {}, Invoice Date {}, Invoice Number {}", issuerVATNumber,invoiceDate, invoiceNumber );
        List<Invoices> duplicateInvoices = invoicesRepository.findDuplicateInvoices( issuerVATNumber, invoiceDate, invoiceNumber );
        if ( !duplicateInvoices.isEmpty() ) {
            log.info( "Found {} duplicate invoices by Issuer VAT Number {}, Invoice Date {}, Invoice Number {}", duplicateInvoices.size(), issuerVATNumber,invoiceDate, invoiceNumber );
        }
        return Optional.of( duplicateInvoices );
    }


    private boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private boolean isSearchCostCenterInvalid(String searchCostCenter, String[] userDedicatedCostCenters) {
        if (userDedicatedCostCenters == null || userDedicatedCostCenters.length == 0) {
            return true;
        }
        return searchCostCenter != null
            && !searchCostCenter.isEmpty()
            && !isUserAuthorized(searchCostCenter, userDedicatedCostCenters);
    }

    private static boolean isUserAuthorized(String searchCostCenter, String[] costCenters) {
        return Arrays.stream(costCenters)
            .anyMatch(costCenter -> costCenter.equalsIgnoreCase(searchCostCenter));
    }

}
