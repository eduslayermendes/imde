package com.example.ImageHandling.domains.repository;

import com.example.ImageHandling.domains.CostCenter;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 1/31/2025
 */
@Repository
public interface CostCenterRepository extends MongoRepository<CostCenter, String> {

	Optional<CostCenter> findByName( String name );
	List<CostCenter> findAllByActiveIsTrue();

}

