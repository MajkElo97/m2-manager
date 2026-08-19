package pl.m2manager.manager.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.m2manager.common.exception.BusinessConflictException;
import pl.m2manager.common.exception.ResourceNotFoundException;
import pl.m2manager.manager.dto.request.CreateManagerRequest;
import pl.m2manager.manager.dto.request.UpdateManagerRequest;
import pl.m2manager.manager.dto.response.ManagerResponse;
import pl.m2manager.manager.entity.Manager;
import pl.m2manager.manager.mapper.ManagerMapper;
import pl.m2manager.manager.repository.ManagerRepository;
import pl.m2manager.tenant.TenantContext;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ManagerService {

	private final ManagerRepository managerRepository;
	private final TenantContext tenantContext;
	private final ManagerMapper managerMapper;

	public ManagerService(
			ManagerRepository managerRepository,
			TenantContext tenantContext,
			ManagerMapper managerMapper
	) {
		this.managerRepository = managerRepository;
		this.tenantContext = tenantContext;
		this.managerMapper = managerMapper;
	}

	public List<ManagerResponse> getAll(String search, Boolean active) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		return managerRepository.findAllByOrganizationIdAndFilters(organizationId, normalizeSearch(search), active).stream()
				.map(manager -> toResponse(manager))
				.toList();
	}

	public ManagerResponse getById(UUID managerId) {
		return toResponse(requireManagerInCurrentOrganization(managerId));
	}

	@Transactional
	public ManagerResponse create(CreateManagerRequest request) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		assertUniqueCode(organizationId, request.code(), null);
		assertUniqueName(organizationId, request.name(), null);

		Manager manager = new Manager();
		manager.setOrganizationId(organizationId);
		managerMapper.applyCreate(manager, request);

		try {
			return toResponse(managerRepository.saveAndFlush(manager));
		}
		catch (DataIntegrityViolationException ex) {
			throw conflictFromConstraint(ex);
		}
	}

	@Transactional
	public ManagerResponse update(UUID managerId, UpdateManagerRequest request) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		Manager manager = requireManagerInCurrentOrganization(managerId);
		assertUniqueCode(organizationId, request.code(), managerId);
		assertUniqueName(organizationId, request.name(), managerId);
		managerMapper.applyUpdate(manager, request);

		try {
			return toResponse(managerRepository.save(manager));
		}
		catch (DataIntegrityViolationException ex) {
			throw conflictFromConstraint(ex);
		}
	}

	@Transactional
	public void deactivate(UUID managerId) {
		Manager manager = requireManagerInCurrentOrganization(managerId);
		if (!manager.isActive()) {
			return;
		}
		manager.setActive(false);
		managerRepository.save(manager);
	}

	private Manager requireManagerInCurrentOrganization(UUID managerId) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		return managerRepository.findByIdAndOrganizationId(managerId, organizationId)
				.orElseThrow(() -> new ResourceNotFoundException("Manager", managerId));
	}

	private void assertUniqueCode(UUID organizationId, String code, UUID excludeManagerId) {
		managerRepository.findByOrganizationIdAndCode(organizationId, code).ifPresent(existing -> {
			if (excludeManagerId == null || !existing.getId().equals(excludeManagerId)) {
				throw new BusinessConflictException("Manager code already exists in organization");
			}
		});
	}

	private void assertUniqueName(UUID organizationId, String name, UUID excludeManagerId) {
		managerRepository.findByOrganizationIdAndName(organizationId, name).ifPresent(existing -> {
			if (excludeManagerId == null || !existing.getId().equals(excludeManagerId)) {
				throw new BusinessConflictException("Manager name already exists in organization");
			}
		});
	}

	private ManagerResponse toResponse(Manager manager) {
		int supervisorCount = (int) managerRepository.countSupervisorsByManagerId(manager.getId());
		return managerMapper.toResponse(manager, supervisorCount);
	}

	private BusinessConflictException conflictFromConstraint(DataIntegrityViolationException ex) {
		String message = ex.getMostSpecificCause().getMessage();
		if (message != null && message.contains("uq_managers_organization_name")) {
			return new BusinessConflictException("Manager name already exists in organization");
		}
		return new BusinessConflictException("Manager code already exists in organization");
	}

	private String normalizeSearch(String search) {
		if (search == null) {
			return null;
		}
		String trimmed = search.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
