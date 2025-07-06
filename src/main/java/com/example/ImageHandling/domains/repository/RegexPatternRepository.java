package com.example.ImageHandling.domains.repository;

import com.example.ImageHandling.domains.RegexPattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegexPatternRepository extends MongoRepository<RegexPattern, String> {
    Optional<RegexPattern> findByName(String name);

    Optional<RegexPattern> findById(String id);

    Page<RegexPattern> findAll( Pageable pageable );

    boolean existsByName(String name);

    @Query("{ 'issuerIds' : ?0 }")
    List<RegexPattern> findByIssuerIdsContainingIssuerId(String issuerId);
}
