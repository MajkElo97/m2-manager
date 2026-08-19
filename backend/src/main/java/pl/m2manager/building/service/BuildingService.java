package pl.m2manager.building.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.m2manager.building.dto.request.CreateBuildingRequest;
import pl.m2manager.building.dto.request.UpdateBuildingRequest;
import pl.m2manager.building.dto.response.BuildingResponse;
import pl.m2manager.building.entity.Building;
import pl.m2manager.building.entity.BuildingStatus;
import pl.m2manager.employee.entity.Employee;
import pl.m2manager.employee.repository.EmployeeRepository;
import pl.m2manager.manager.entity.Manager;
import pl.m2manager.manager.repository.ManagerRepository;
import pl.m2manager.supervisor.entity.Supervisor;
import pl.m2manager.supervisor.repository.SupervisorRepository;
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
	private final ManagerRepository managerRepository;
	private final SupervisorRepository supervisorRepository;
	private final EmployeeRepository employeeRepository;
	private final TenantContext tenantContext;
	private final BuildingMapper buildingMapper;

	public BuildingService(
			BuildingRepository buildingRepository,
			OrganizationRepository organizationRepository,
			ManagerRepository managerRepository,
			SupervisorRepository supervisorRepository,
			EmployeeRepository employeeRepository,
			TenantContext tenantContext,
			BuildingMapper buildingMapper
	) {
		this.buildingRepository = buildingRepository;
		this.organizationRepository = organizationRepository;
		this.managerRepository = managerRepository;
		this.supervisorRepository = supervisorRepository;
		this.employeeRepository = employeeRepository;
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
		applyPersonnelReferences(building, organizationId, request.managerCode(), request.supervisorCode(), request.employeeCode());

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
		applyPersonnelReferences(building, organizationId, request.managerCode(), request.supervisorCode(), request.employeeCode());

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

	private void applyPersonnelReferences(
			Building building,
			UUID organizationId,
			String managerCode,
			String supervisorCode,
			String employeeCode
	) {
		String normalizedManagerCode = normalizeOptionalCode(managerCode);
		String normalizedSupervisorCode = normalizeOptionalCode(supervisorCode);
		String normalizedEmployeeCode = normalizeOptionalCode(employeeCode);

		Manager manager = resolveManager(organizationId, normalizedManagerCode);
		Supervisor supervisor = resolveSupervisor(organizationId, normalizedSupervisorCode);
		Employee employee = resolveEmployee(organizationId, normalizedEmployeeCode);

		if (supervisor != null && manager != null && !supervisor.getManagerId().equals(manager.getId())) {
			throw new BusinessConflictException("Supervisor does not belong to the selected manager");
		}

		building.setManagerCode(normalizedManagerCode);
		building.setSupervisorCode(normalizedSupervisorCode);
		building.setEmployeeCode(normalizedEmployeeCode);
		building.setManager(manager);
		building.setSupervisor(supervisor);
		building.setEmployee(employee);
	}

	private Manager resolveManager(UUID organizationId, String code) {
		if (code == null) {
			return null;
		}
		return managerRepository.findByOrganizationIdAndCode(organizationId, code)
				.orElseThrow(() -> new BusinessConflictException("Manager not found in organization"));
	}

	private Supervisor resolveSupervisor(UUID organizationId, String code) {
		if (code == null) {
			return null;
		}
		return supervisorRepository.findByOrganizationIdAndCode(organizationId, code)
				.orElseThrow(() -> new BusinessConflictException("Supervisor not found in organization"));
	}

	private Employee resolveEmployee(UUID organizationId, String code) {
		if (code == null) {
			return null;
		}
		return employeeRepository.findByOrganizationIdAndCode(organizationId, code)
				.orElseThrow(() -> new BusinessConflictException("Employee not found in organization"));
	}

	private String normalizeOptionalCode(String code) {
		if (code == null) {
			return null;
		}
		String trimmed = code.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
