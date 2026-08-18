package pl.m2manager.building.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.m2manager.building.dto.request.CreateBuildingRequest;
import pl.m2manager.building.dto.request.UpdateBuildingRequest;
import pl.m2manager.building.dto.response.BuildingResponse;
import pl.m2manager.building.entity.Building;
import pl.m2manager.building.entity.BuildingStatus;
import pl.m2manager.building.mapper.BuildingMapper;
import pl.m2manager.building.repository.BuildingRepository;
import pl.m2manager.common.exception.BusinessConflictException;
import pl.m2manager.common.exception.ResourceNotFoundException;
import pl.m2manager.organization.entity.Organization;
import pl.m2manager.organization.repository.OrganizationRepository;
import pl.m2manager.tenant.TenantContext;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class BuildingService {

	private final BuildingRepository buildingRepository;
	private final OrganizationRepository organizationRepository;
	private final TenantContext tenantContext;
	private final BuildingMapper buildingMapper;

	public BuildingService(
			BuildingRepository buildingRepository,
			OrganizationRepository organizationRepository,
			TenantContext tenantContext,
			BuildingMapper buildingMapper
	) {
		this.buildingRepository = buildingRepository;
		this.organizationRepository = organizationRepository;
		this.tenantContext = tenantContext;
		this.buildingMapper = buildingMapper;
	}

	public List<BuildingResponse> getAll(BuildingStatus status, String search) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		String normalizedSearch = normalizeSearch(search);
		return buildingRepository.findAllByOrganizationIdAndFilters(organizationId, status, normalizedSearch).stream()
				.map(buildingMapper::toResponse)
				.toList();
	}

	public BuildingResponse getById(UUID buildingId) {
		return buildingMapper.toResponse(requireBuildingInCurrentOrganization(buildingId));
	}

	@Transactional
	public BuildingResponse create(CreateBuildingRequest request) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		assertUniqueCode(organizationId, request.code(), null);
		assertValidDateRange(request.contractSignedAt(), request.serviceStartDate());

		Organization organization = organizationRepository.findById(organizationId)
				.orElseThrow(() -> new ResourceNotFoundException("Organization", organizationId));

		Building building = new Building();
		building.setOrganization(organization);
		buildingMapper.applyCreate(building, request);

		try {
			return buildingMapper.toResponse(buildingRepository.saveAndFlush(building));
		}
		catch (DataIntegrityViolationException ex) {
			throw new BusinessConflictException("Building code already exists in organization");
		}
	}

	@Transactional
	public BuildingResponse update(UUID buildingId, UpdateBuildingRequest request) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		Building building = requireBuildingInCurrentOrganization(buildingId);
		assertUniqueCode(organizationId, request.code(), buildingId);
		assertValidDateRange(request.contractSignedAt(), request.serviceStartDate());

		buildingMapper.applyUpdate(building, request);

		try {
			return buildingMapper.toResponse(buildingRepository.save(building));
		}
		catch (DataIntegrityViolationException ex) {
			throw new BusinessConflictException("Building code already exists in organization");
		}
	}

	@Transactional
	public void deactivate(UUID buildingId) {
		Building building = requireBuildingInCurrentOrganization(buildingId);
		if (building.getStatus() == BuildingStatus.INACTIVE) {
			return;
		}
		building.setStatus(BuildingStatus.INACTIVE);
		buildingRepository.save(building);
	}

	private Building requireBuildingInCurrentOrganization(UUID buildingId) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		return buildingRepository.findByIdAndOrganizationId(buildingId, organizationId)
				.orElseThrow(() -> new ResourceNotFoundException("Building", buildingId));
	}

	private void assertUniqueCode(UUID organizationId, String code, UUID excludeBuildingId) {
		buildingRepository.findByOrganizationIdAndCode(organizationId, code).ifPresent(existing -> {
			if (excludeBuildingId == null || !existing.getId().equals(excludeBuildingId)) {
				throw new BusinessConflictException("Building code already exists in organization");
			}
		});
	}

	private void assertValidDateRange(LocalDate contractSignedAt, LocalDate serviceStartDate) {
		if (contractSignedAt != null && serviceStartDate != null && serviceStartDate.isBefore(contractSignedAt)) {
			throw new BusinessConflictException("Service start date cannot be before contract signed date");
		}
	}

	private String normalizeSearch(String search) {
		if (search == null) {
			return null;
		}
		String trimmed = search.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
