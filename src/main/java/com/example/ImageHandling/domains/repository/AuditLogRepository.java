package com.example.ImageHandling.domains.repository;

import com.example.ImageHandling.domains.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, Long> {

	Page<AuditLog> findAll( Pageable pageable );
}
