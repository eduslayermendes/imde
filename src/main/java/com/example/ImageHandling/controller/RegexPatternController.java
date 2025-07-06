package com.example.ImageHandling.controller;

import com.example.ImageHandling.domains.RegexPattern;
import com.example.ImageHandling.domains.dto.ErrorResponse;
import com.example.ImageHandling.services.RegexPatternService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.example.ImageHandling.utils.ResponseUtil.createErrorResponse;
import static com.example.ImageHandling.utils.SortUtil.getSortingOrders;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/regex-patterns")
@PreAuthorize("hasAnyAuthority(@layoutsAllowedRoles)")
public class RegexPatternController {

    private static final Logger logger = LoggerFactory.getLogger(RegexPatternController.class);

    private final RegexPatternService regexPatternService;

    /**
     * Endpoint to update a regex pattern.
     * This method updates the regex pattern with the specified ID.
     * @param id The ID of the regex pattern to update
     * @param pattern The updated RegexPattern object
     * @return Response entity containing the updated regex pattern
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updatePattern(@PathVariable String id, @RequestBody RegexPattern pattern) {
            RegexPattern updatedPattern = regexPatternService.updatePattern(id, pattern);
            return ResponseEntity.ok(updatedPattern);
    }

    /**
     * Endpoint to delete regex patterns by ID
     * @param id The ID of the pattern
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteLayout(@PathVariable String id) {
            regexPatternService.deleteLayout(id);
            return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint to retrieve regex patterns by issuer ID.
     * This method returns a list of regex patterns associated with the specified issuer ID.
     * @param issuerId The ID of the issuer
     * @return Response entity containing a list of regex patterns
     */
    @GetMapping("/issuer/{issuerId}")
    public ResponseEntity<?> getPatternsByIssuerId(@PathVariable String issuerId) throws NoSuchFieldException {
            List<RegexPattern> patterns = regexPatternService.getPatternsByIssuerId(issuerId);
            return ResponseEntity.ok(patterns);

    }

    /**
     * Endpoint to save a new regex pattern.
     * This method creates and saves a new regex pattern.
     * @param pattern The RegexPattern object to save
     * @return Response entity containing the saved regex pattern
     */
    @PostMapping("/save")
    public ResponseEntity<?> createPattern(@RequestBody RegexPattern pattern) {
            RegexPattern savedPattern = regexPatternService.createRegexPattern(pattern);
            return ResponseEntity.ok(savedPattern);

    }

    /**
     * Endpoint to retrieve all regex patterns.
     * This method returns a list of all regex patterns.
     * @return Response entity containing a list of all regex patterns
     */
    @GetMapping("/get")
    public ResponseEntity<?> getPatterns(
        @RequestParam( defaultValue = "0" ) Integer pageNo,
        @RequestParam( defaultValue = "10" ) Integer pageSize,
        @RequestParam( defaultValue = "createdAt,desc" ) String[] sort
    ) {
            List<RegexPattern> patterns = regexPatternService.getAllPatternsWithPaginationSorting(pageNo, pageSize, Sort.by(getSortingOrders(sort)));
            return ResponseEntity.ok(patterns);
    }

    @GetMapping( "/{id}" )
    public ResponseEntity<?> getPatternById( @PathVariable String id ) {
            Optional<RegexPattern> regexPattern = regexPatternService.getPatternById( id );
            if ( regexPattern.isPresent() ) {
                return ResponseEntity.ok( regexPattern.get() );
            }
        return ResponseEntity.status( HttpStatus.NO_CONTENT ).body( "Pattern not found with id: " + id );

    }


    @GetMapping("/count")
    public ResponseEntity<?> count() {
            return ResponseEntity.ok( regexPatternService.getLayoutsCount() ) ;

    }
}
