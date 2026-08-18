package pl.m2manager.staircase.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.m2manager.building.repository.BuildingRepository;
import pl.m2manager.common.exception.BusinessConflictException;
import pl.m2manager.common.exception.ResourceNotFoundException;
import pl.m2manager.staircase.dto.request.CreateStaircaseRequest;
import pl.m2manager.staircase.dto.request.UpdateStaircaseRequest;
import pl.m2manager.staircase.dto.response.StaircaseResponse;
import pl.m2manager.staircase.entity.Staircase;
import pl.m2manager.staircase.mapper.StaircaseMapper;
import pl.m2manager.staircase.repository.StaircaseRepository;
import pl.m2manager.tenant.TenantContext;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class StaircaseService {

	private final StaircaseRepository staircaseRepository;
	private final BuildingRepository buildingRepository;
	private final TenantContext tenantContext;
	private final StaircaseMapper staircaseMapper;

	public StaircaseService(
			StaircaseRepository staircaseRepository,
			BuildingRepository buildingRepository,
			TenantContext tenantContext,
			StaircaseMapper staircaseMapper
	) {
		this.staircaseRepository = staircaseRepository;
		this.buildingRepository = buildingRepository;
		this.tenantContext = tenantContext;
		this.staircaseMapper = staircaseMapper;
	}

	public List<StaircaseResponse> getAll(UUID buildingId) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		requireBuildingInCurrentOrganization(buildingId, organizationId);
		return staircaseRepository.findAllByOrganizationIdAndBuildingIdOrderByDesignationAsc(organizationId, buildingId).stream()
				.map(staircaseMapper::toResponse)
				.toList();
	}

	public StaircaseResponse getById(UUID staircaseId) {
		return staircaseMapper.toResponse(requireStaircaseInCurrentOrganization(staircaseId));
	}

	@Transactional
	public StaircaseResponse create(CreateStaircaseRequest request) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		requireBuildingInCurrentOrganization(request.buildingId(), organizationId);
		assertUniqueCode(organizationId, request.code(), null);
		assertUniqueDesignation(request.buildingId(), request.designation(), null);

		Staircase staircase = new Staircase();
		staircase.setOrganizationId(organizationId);
		staircase.setBuildingId(request.buildingId());
		staircaseMapper.applyCreate(staircase, request);

		try {
			return staircaseMapper.toResponse(staircaseRepository.saveAndFlush(staircase));
		}
		catch (DataIntegrityViolationException ex) {
			throw conflictFromConstraint(ex);
		}
	}

	@Transactional
	public StaircaseResponse update(UUID staircaseId, UpdateStaircaseRequest request) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		Staircase staircase = requireStaircaseInCurrentOrganization(staircaseId);
		assertUniqueCode(organizationId, request.code(), staircaseId);
		assertUniqueDesignation(staircase.getBuildingId(), request.designation(), staircaseId);

		staircaseMapper.applyUpdate(staircase, request);

		try {
			return staircaseMapper.toResponse(staircaseRepository.save(staircase));
		}
		catch (DataIntegrityViolationException ex) {
			throw conflictFromConstraint(ex);
		}
	}

	@Transactional
	public void delete(UUID staircaseId) {
		Staircase staircase = requireStaircaseInCurrentOrganization(staircaseId);
		staircaseRepository.delete(staircase);
	}

	private Staircase requireStaircaseInCurrentOrganization(UUID staircaseId) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		return staircaseRepository.findByIdAndOrganizationId(staircaseId, organizationId)
				.orElseThrow(() -> new ResourceNotFoundException("Staircase", staircaseId));
	}

	private void requireBuildingInCurrentOrganization(UUID buildingId, UUID organizationId) {
		buildingRepository.findByIdAndOrganizationId(buildingId, organizationId)
				.orElseThrow(() -> new ResourceNotFoundException("Building", buildingId));
	}

	private void assertUniqueCode(UUID organizationId, String code, UUID excludeStaircaseId) {
		staircaseRepository.findByOrganizationIdAndCode(organizationId, code).ifPresent(existing -> {
			if (excludeStaircaseId == null || !existing.getId().equals(excludeStaircaseId)) {
				throw new BusinessConflictException("Staircase code already exists in organization");
			}
		});
	}

	private void assertUniqueDesignation(UUID buildingId, String designation, UUID excludeStaircaseId) {
		staircaseRepository.findByBuildingIdAndDesignation(buildingId, designation).ifPresent(existing -> {
			if (excludeStaircaseId == null || !existing.getId().equals(excludeStaircaseId)) {
				throw new BusinessConflictException("Staircase designation already exists in building");
			}
		});
	}

	private BusinessConflictException conflictFromConstraint(DataIntegrityViolationException ex) {
		String message = ex.getMostSpecificCause().getMessage();
		if (message != null && message.contains("uq_staircases_building_designation")) {
			return new BusinessConflictException("Staircase designation already exists in building");
		}
		return new BusinessConflictException("Staircase code already exists in organization");
	}
}
