package com.example.ImageHandling.domains.repository;

import com.example.ImageHandling.domains.ExtraDocumentType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 3/12/2025
 */
@Repository
public interface ExtraDocumentTypeRepository extends MongoRepository<ExtraDocumentType, String> {

	Optional<ExtraDocumentType> findByName( String name );

}
