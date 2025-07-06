package com.example.ImageHandling.services;

import com.example.ImageHandling.domains.CostCenter;
import com.example.ImageHandling.domains.repository.CostCenterRepository;
import com.example.ImageHandling.domains.types.LogOperation;
import com.example.ImageHandling.exception.DuplicateCostCenterException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 1/31/2025
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CostCenterService {
	
	
	private final CostCenterRepository costCenterRepository;
	private static final Logger logger = LoggerFactory.getLogger(CostCenterService.class);
	private final AuthService authService;
	private final AuditLogService auditLogService;




	public List<CostCenter> findAll() {
		logger.info("Fetching all cost centers");
		return costCenterRepository.findAll();
	}

	public List<CostCenter> findActiveCostCenters() {
		logger.info("Fetching all active cost centers");
		return costCenterRepository.findAllByActiveIsTrue();
	}

	public List<CostCenter> findUserActiveCostCenters() {
		logger.info("Fetching all active cost centers for the logged in user");
		String[] userCostCenters = authService.getLoggedInUserDetails().getCostCenters();
		if ( userCostCenters != null ) {
			return costCenterRepository.findAllByActiveIsTrue().stream()
				.filter( costCenter -> List.of( userCostCenters ).contains( costCenter.getName() ) )
				.toList();
		}

		return List.of();
	}

	public Optional<CostCenter> findById(String id) {
		logger.info("Fetching cost center with id: {}", id);
		return costCenterRepository.findById(id);
	}


	public CostCenter save( CostCenter costCenter) {
		logger.info("Saving cost center: {}", costCenter);

		CostCenter savedCostCenter = costCenterRepository.save( costCenter );
		if ( savedCostCenter != null ) {
			logger.info("Saved cost center successfully: {}", savedCostCenter);
			auditLogService.persistAuditLog(savedCostCenter, LogOperation.COST_CENTER_CREATED);
			return savedCostCenter;
		}
		return null;
	}

	public CostCenter saveOrUpdate(CostCenter costCenter){
		logger.info("Saving or updating cost center: {}", costCenter);
		if ( costCenter.getId() != null ) {
			Optional<CostCenter> foundCostCenter = costCenterRepository.findById( costCenter.getId() );
			if ( foundCostCenter.isPresent() ) {
				CostCenter existingCostCenter = foundCostCenter.get();
				existingCostCenter.setActive( costCenter.getActive() );
				existingCostCenter.setDescription( costCenter.getDescription() );
				CostCenter updatedCostCenter = costCenterRepository.save( existingCostCenter );
				auditLogService.persistAuditLog(updatedCostCenter, LogOperation.COST_CENTER_UPDATED);
				logger.info("Updated cost center successfully: {}", updatedCostCenter);
				return updatedCostCenter;
			} else {
				throw new IllegalArgumentException("Cost center with id " + costCenter.getId() + " not found");
			}
		}
		else {

			Optional.ofNullable( costCenter.getName() ).ifPresent( name -> {
				costCenterRepository.findByName( name.trim() ).ifPresent( foundCostCenter -> {
					throw new DuplicateCostCenterException("Cost center with name " + name + " already exists");
				});
			});

			CostCenter savedCostCenter = costCenterRepository.save( costCenter );
			auditLogService.persistAuditLog(savedCostCenter, LogOperation.COST_CENTER_CREATED);
			logger.info("Saved cost center successfully: {}", savedCostCenter);
			return savedCostCenter;
		}

	}
	





}
