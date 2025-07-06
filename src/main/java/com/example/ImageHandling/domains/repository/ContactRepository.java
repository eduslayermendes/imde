package com.example.ImageHandling.domains.repository;

import com.example.ImageHandling.domains.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactRepository extends MongoRepository<Contact, String> {
	Page<Contact> findAll( Pageable pageable );
}
