// IssuerRepository.java
package com.example.ImageHandling.domains.repository;

import com.example.ImageHandling.domains.Issuer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IssuerRepository extends MongoRepository<Issuer, String> {
	Page<Issuer> findAll( Pageable pageable );
	Optional<List<Issuer>> findByLayoutIdsContaining(String layoutId);
	Optional<Issuer> findByName(String name);
}
