package com.example.ImageHandling.domains.repository;

import com.example.ImageHandling.domains.ExtraDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 2/21/2025
 */
@Repository
public interface ExtraDocumentsRepository extends MongoRepository<ExtraDocument, String> {

}
