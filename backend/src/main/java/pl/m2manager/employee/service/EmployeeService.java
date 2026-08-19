package pl.m2manager.employee.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.m2manager.common.exception.BusinessConflictException;
import pl.m2manager.common.exception.ResourceNotFoundException;
import pl.m2manager.employee.dto.request.CreateEmployeeRequest;
import pl.m2manager.employee.dto.request.UpdateEmployeeRequest;
import pl.m2manager.employee.dto.response.EmployeeResponse;
import pl.m2manager.employee.entity.Employee;
import pl.m2manager.employee.entity.EmployeeRole;
import pl.m2manager.employee.entity.EmploymentType;
import pl.m2manager.employee.mapper.EmployeeMapper;
import pl.m2manager.employee.repository.EmployeeRepository;
import pl.m2manager.organization.entity.Organization;
import pl.m2manager.organization.repository.OrganizationRepository;
import pl.m2manager.tenant.TenantContext;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class EmployeeService {

	private final EmployeeRepository employeeRepository;
	private final OrganizationRepository organizationRepository;
	private final TenantContext tenantContext;
	private final EmployeeMapper employeeMapper;

	public EmployeeService(
			EmployeeRepository employeeRepository,
			OrganizationRepository organizationRepository,
			TenantContext tenantContext,
			EmployeeMapper employeeMapper
	) {
		this.employeeRepository = employeeRepository;
		this.organizationRepository = organizationRepository;
		this.tenantContext = tenantContext;
		this.employeeMapper = employeeMapper;
	}

	public List<EmployeeResponse> getAll(
			String search,
			String position,
			EmployeeRole role,
			EmploymentType employmentType,
			Boolean active
	) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		return employeeRepository.findAllByOrganizationIdAndFilters(
				organizationId,
				normalizeSearch(search),
				normalize(position),
				role,
				employmentType,
				active
		).stream()
				.map(employeeMapper::toResponse)
				.toList();
	}

	public EmployeeResponse getById(UUID employeeId) {
		return employeeMapper.toResponse(requireEmployeeInCurrentOrganization(employeeId));
	}

	@Transactional
	public EmployeeResponse create(CreateEmployeeRequest request) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		assertUniqueCode(organizationId, request.code(), null);

		Organization organization = organizationRepository.findById(organizationId)
				.orElseThrow(() -> new ResourceNotFoundException("Organization", organizationId));

		Employee employee = new Employee();
		employee.setOrganization(organization);
		employeeMapper.applyCreate(employee, request);

		try {
			return employeeMapper.toResponse(employeeRepository.saveAndFlush(employee));
		}
		catch (DataIntegrityViolationException ex) {
			throw new BusinessConflictException("Employee code already exists in organization");
		}
	}

	@Transactional
	public EmployeeResponse update(UUID employeeId, UpdateEmployeeRequest request) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		Employee employee = requireEmployeeInCurrentOrganization(employeeId);
		assertUniqueCode(organizationId, request.code(), employeeId);

		employeeMapper.applyUpdate(employee, request);

		try {
			return employeeMapper.toResponse(employeeRepository.save(employee));
		}
		catch (DataIntegrityViolationException ex) {
			throw new BusinessConflictException("Employee code already exists in organization");
		}
	}

	@Transactional
	public void deactivate(UUID employeeId) {
		Employee employee = requireEmployeeInCurrentOrganization(employeeId);
		if (!employee.isActive()) {
			return;
		}
		employee.setActive(false);
		employeeRepository.save(employee);
	}

	private Employee requireEmployeeInCurrentOrganization(UUID employeeId) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		return employeeRepository.findByIdAndOrganizationId(employeeId, organizationId)
				.orElseThrow(() -> new ResourceNotFoundException("Employee", employeeId));
	}

	private void assertUniqueCode(UUID organizationId, String code, UUID excludeEmployeeId) {
		employeeRepository.findByOrganizationIdAndCode(organizationId, code).ifPresent(existing -> {
			if (excludeEmployeeId == null || !existing.getId().equals(excludeEmployeeId)) {
				throw new BusinessConflictException("Employee code already exists in organization");
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

	private String normalize(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
