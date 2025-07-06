package com.example.ImageHandling.domains.repository;

import com.example.ImageHandling.domains.BatchProcessFile;
import com.example.ImageHandling.domains.Invoices;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatchProcessFilesRepository extends MongoRepository<BatchProcessFile, String> {
    List<BatchProcessFile> findByProcessId(String processId);
    List<BatchProcessFile> findAllByProcessIdIn(List<String> processIds);
}
