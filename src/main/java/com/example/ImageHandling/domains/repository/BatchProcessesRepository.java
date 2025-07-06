package com.example.ImageHandling.domains.repository;

import com.example.ImageHandling.domains.BatchProcess;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BatchProcessesRepository extends MongoRepository<BatchProcess, String> {

	Page<BatchProcess> findAll( Pageable pageable );

	List<BatchProcess> findByExpirationDateBefore( LocalDateTime now );
}
