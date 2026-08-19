package pl.m2manager.supervisor.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.m2manager.common.exception.BusinessConflictException;
import pl.m2manager.common.exception.ResourceNotFoundException;
import pl.m2manager.manager.repository.ManagerRepository;
import pl.m2manager.supervisor.dto.request.CreateSupervisorRequest;
import pl.m2manager.supervisor.dto.request.UpdateSupervisorRequest;
import pl.m2manager.supervisor.dto.response.SupervisorResponse;
import pl.m2manager.supervisor.entity.Supervisor;
import pl.m2manager.supervisor.mapper.SupervisorMapper;
import pl.m2manager.supervisor.repository.SupervisorRepository;
import pl.m2manager.tenant.TenantContext;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class SupervisorService {

	private final SupervisorRepository supervisorRepository;
	private final ManagerRepository managerRepository;
	private final TenantContext tenantContext;
	private final SupervisorMapper supervisorMapper;

	public SupervisorService(
			SupervisorRepository supervisorRepository,
			ManagerRepository managerRepository,
			TenantContext tenantContext,
			SupervisorMapper supervisorMapper
	) {
		this.supervisorRepository = supervisorRepository;
		this.managerRepository = managerRepository;
		this.tenantContext = tenantContext;
		this.supervisorMapper = supervisorMapper;
	}

	public List<SupervisorResponse> getAll(UUID managerId, Boolean active, String search) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		List<Supervisor> supervisors = supervisorRepository.findAllByOrganizationIdAndFilters(
				organizationId,
				managerId,
				active,
				normalizeSearch(search)
		);
		return supervisorMapper.toResponseList(supervisors, organizationId);
	}

	public SupervisorResponse getById(UUID supervisorId) {
		return supervisorMapper.toResponse(requireSupervisorInCurrentOrganization(supervisorId));
	}

	@Transactional
	public SupervisorResponse create(CreateSupervisorRequest request) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		requireManagerInCurrentOrganization(request.managerId(), organizationId);
		assertUniqueCode(organizationId, request.code(), null);

		Supervisor supervisor = new Supervisor();
		supervisor.setOrganizationId(organizationId);
		supervisorMapper.applyCreate(supervisor, request);

		try {
			return supervisorMapper.toResponse(supervisorRepository.saveAndFlush(supervisor));
		}
		catch (DataIntegrityViolationException ex) {
			throw new BusinessConflictException("Supervisor code already exists in organization");
		}
	}

	@Transactional
	public SupervisorResponse update(UUID supervisorId, UpdateSupervisorRequest request) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		Supervisor supervisor = requireSupervisorInCurrentOrganization(supervisorId);
		requireManagerInCurrentOrganization(request.managerId(), organizationId);
		assertUniqueCode(organizationId, request.code(), supervisorId);
		supervisorMapper.applyUpdate(supervisor, request);

		try {
			return supervisorMapper.toResponse(supervisorRepository.save(supervisor));
		}
		catch (DataIntegrityViolationException ex) {
			throw new BusinessConflictException("Supervisor code already exists in organization");
		}
	}

	@Transactional
	public void deactivate(UUID supervisorId) {
		Supervisor supervisor = requireSupervisorInCurrentOrganization(supervisorId);
		if (!supervisor.isActive()) {
			return;
		}
		supervisor.setActive(false);
		supervisorRepository.save(supervisor);
	}

	private Supervisor requireSupervisorInCurrentOrganization(UUID supervisorId) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		return supervisorRepository.findByIdAndOrganizationId(supervisorId, organizationId)
				.orElseThrow(() -> new ResourceNotFoundException("Supervisor", supervisorId));
	}

	private void requireManagerInCurrentOrganization(UUID managerId, UUID organizationId) {
		managerRepository.findByIdAndOrganizationId(managerId, organizationId)
				.orElseThrow(() -> new ResourceNotFoundException("Manager", managerId));
	}

	private void assertUniqueCode(UUID organizationId, String code, UUID excludeSupervisorId) {
		supervisorRepository.findByOrganizationIdAndCode(organizationId, code).ifPresent(existing -> {
			if (excludeSupervisorId == null || !existing.getId().equals(excludeSupervisorId)) {
				throw new BusinessConflictException("Supervisor code already exists in organization");
			}
		});
	}

	private String normalizeSearch(String search) {
		if (search == null) {
			return null;
		}
		String trimmed = search.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
