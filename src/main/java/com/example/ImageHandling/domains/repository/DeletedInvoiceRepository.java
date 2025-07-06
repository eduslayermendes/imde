package com.example.ImageHandling.domains.repository;

import com.example.ImageHandling.domains.DeletedInvoice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeletedInvoiceRepository extends MongoRepository<DeletedInvoice, String> {
}
