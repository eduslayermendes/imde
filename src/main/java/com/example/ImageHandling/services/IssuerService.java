package com.example.ImageHandling.services;

import com.example.ImageHandling.domains.Issuer;
import com.example.ImageHandling.domains.RegexPattern;
import com.example.ImageHandling.domains.RegexPatternDeletedEvent;
import com.example.ImageHandling.domains.repository.IssuerRepository;
import com.example.ImageHandling.domains.repository.RegexPatternRepository;
import com.example.ImageHandling.exception.DuplicateIssuerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import static com.example.ImageHandling.domains.types.LogOperation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IssuerService {

    private final IssuerRepository issuerRepository;
    private final RegexPatternRepository regexPatternRepository;
    private final AuditLogService auditLogService;
    private final AuthService authService;
    private final MongoTemplate mongoTemplate;
    private static final Logger logger = LoggerFactory.getLogger(IssuerService.class);

    @Transactional
    public Issuer createIssuer(Issuer issuer) {
        logger.info("Creating issuer: {}", issuer);
        if (issuer == null || issuer.getName() == null || issuer.getName().isEmpty()) {
            throw new IllegalArgumentException("Issuer name must not be null or empty.");
        }

        Optional<Issuer> existingIssuer = issuerRepository.findByName(issuer.getName());

        if (existingIssuer.isPresent()) {
            throw new DuplicateIssuerException("Issuer with name " + issuer.getName() + " already exists.");
        }

        issuer.setCreatedAt(LocalDateTime.now());
        issuer.setCreatedBy(authService.getLoggedInUserDetails().getUsername());
        Issuer createdIssuer = issuerRepository.save(issuer);

        auditLogService.logAction(
            ISSUER_CREATED.toString(),
            authService.getLoggedInUserDetails().getUsername(),
            "Issuer " + createdIssuer.getName()
        );

        return createdIssuer;
    }

    public Issuer deleteIssuer(String id) {
        Optional<Issuer> issuerOpt = issuerRepository.findById(id);
        if (issuerOpt.isPresent()) {
            Issuer issuer = issuerOpt.get();
            issuerRepository.deleteById(id);
            logger.info("Issuer deleted: {}", id);
            return issuer;
        } else {
            logger.error("Issuer {} not found", id);
            return null;
        }
    }

    public List<Issuer> getAllIssuers() {
        logger.info("Fetching all issuers from the database");
        return issuerRepository.findAll();
    }

    public List<Issuer> getAllIssuersWithPaginationSorting(Integer pageNo, Integer pageSize, Sort sort) {
        logger.info("Fetching issuers with pagination and sorting - PageNo: {}, PageSize: {}, Sort: {}", pageNo, pageSize, sort);
        Pageable paging = PageRequest.of(pageNo, pageSize, sort);
        Page<Issuer> pageResult = issuerRepository.findAll( paging );
        if (pageResult.hasContent()) {
            return pageResult.getContent();
        } else {
            return new ArrayList<>();
        }
    }


    public List<Issuer> getAllIssuersWithCaseInsensitiveSorting(Integer pageNo, Integer pageSize, Sort sort) {
        logger.info("Fetching all issuers with case insensitive sorting - PageNo: {}, PageSize: {}", pageNo, pageSize);

        if (sort == null || sort.isEmpty()) {
            logger.warn("Sort field is null or empty, defaulting to 'name' sorting");
            sort = Sort.by(Sort.Order.asc("name"));
        }

        Sort.Order order = sort.iterator().next();
        String sortField = order.getProperty();
        Sort.Direction sortDirection = order.getDirection();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        if ("name".equalsIgnoreCase(sortField)) {

            Query query = new Query().with(pageable);
            query.with(Sort.by(sortDirection.isAscending() ? Sort.Order.asc("name") : Sort.Order.desc("name")));
            query.collation(Collation.of("en").strength(Collation.ComparisonLevel.secondary())); // Case-insensitive sorting

            List<Issuer> issuers = mongoTemplate.find(query, Issuer.class);
            if (issuers.isEmpty()) {
                logger.info("No issuers found for case-insensitive sorting by 'name'");
            }
            return issuers;
        } else {

            Page<Issuer> pageResult = issuerRepository.findAll(pageable);
            if (pageResult.hasContent()) {
                return pageResult.getContent();
            } else {
                logger.info("No issuers found for sorting by '{}'", sortField);
                return new ArrayList<>();
            }
        }
    }


    public Optional<Issuer> getIssuerById(String id) {
        logger.info("Fetching issuer with id: {}", id);
        return issuerRepository.findById(id);
    }

    public Issuer updateIssuer(String id, Issuer issuer) {
        logger.info("Updating issuer with id: {}", id);
        Optional<Issuer> existingIssuerOpt = issuerRepository.findById(id);
        if (existingIssuerOpt.isPresent()) {
            Issuer existingIssuer = existingIssuerOpt.get();
            Issuer oldIssuer = new Issuer(existingIssuer);

            existingIssuer.setName(issuer.getName());
            existingIssuer.setPt(issuer.isPt());
            existingIssuer.setCreatedBy(issuer.getCreatedBy());
            existingIssuer.setLayoutIds(issuer.getLayoutIds());
            existingIssuer.setUpdatedAt(LocalDateTime.now());

            Issuer updatedIssuer = issuerRepository.save(existingIssuer);

            List<String> oldLayoutNames = toList(regexPatternRepository.findAllById(oldIssuer.getLayoutIds()))
                    .stream()
                    .map(RegexPattern::getName)
                    .collect(Collectors.toList());

            List<String> newLayoutNames = toList(regexPatternRepository.findAllById(updatedIssuer.getLayoutIds()))
                    .stream()
                    .map(RegexPattern::getName)
                    .collect(Collectors.toList());

            logger.info( "Updated issuer {} ({}) successfully!", issuer.getName(), id );

            // Log changes with layout names
            auditLogService.logChanges(ISSUER_UPDATED.toString(), authService.getLoggedInUserDetails().getUsername(), oldIssuer, updatedIssuer, oldLayoutNames, newLayoutNames);

            return updatedIssuer;
        } else {
            logger.error( "Error on updating issuer detail. id: {}", id );
            throw new RuntimeException("Issuer not found");
        }
    }

    // Converte um Iterable para uma List
    private <T> List<T> toList(Iterable<T> iterable) {
        List<T> list = new ArrayList<>();
        iterable.forEach(list::add);
        return list;
    }


    public void deleteAndLogIssuer( String id ) {
        logger.info( "Deleting issuer with ID {}", id );
        Issuer deletedIssuer = deleteIssuer(id);
        if (deletedIssuer != null) {
            auditLogService.logAction(ISSUER_DELETED.toString(), authService.getLoggedInUserDetails().getUsername(), "Issuer " + deletedIssuer.getName());
        } else {
            logger.warn( "Issuer with ID {} not found", id );
            auditLogService.logAction(ISSUER_DELETE_FAILED.toString(), authService.getLoggedInUserDetails().getUsername(), "Issuer with ID " + id + " not found");
        }
    }

    public long getIssuerCount() {
            logger.info( "Issuer count: {}", issuerRepository.count());
            return issuerRepository.count();
    }

    @EventListener
    public void handlePatternDeletedEvent( RegexPatternDeletedEvent event) {
        String patternId = event.getPatternId();
        issuerRepository.findByLayoutIdsContaining( patternId ).ifPresentOrElse( issuers -> {
            issuers.forEach( issuer -> {
                if (issuer.getLayoutIds().contains(patternId)) {
                    issuer.getLayoutIds().remove(patternId);
                    issuerRepository.save(issuer);
                    logger.info( "Issuer layout list is updated due to the deleted layout with id: {}", patternId );
                }
            } );

        }, () -> {
            logger.warn( "Layout with ID  {}  not found in the issuer's layouts to be deleted", patternId );
        } );
    }


}
