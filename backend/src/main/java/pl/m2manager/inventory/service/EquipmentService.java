package pl.m2manager.inventory.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.m2manager.common.exception.BusinessConflictException;
import pl.m2manager.common.exception.ResourceNotFoundException;
import pl.m2manager.employee.entity.Employee;
import pl.m2manager.employee.repository.EmployeeRepository;
import pl.m2manager.inventory.dto.request.CreateEquipmentRequest;
import pl.m2manager.inventory.dto.request.UpdateEquipmentRequest;
import pl.m2manager.inventory.dto.response.EquipmentResponse;
import pl.m2manager.inventory.entity.Equipment;
import pl.m2manager.inventory.entity.EquipmentCondition;
import pl.m2manager.inventory.mapper.EquipmentMapper;
import pl.m2manager.inventory.repository.EquipmentRepository;
import pl.m2manager.tenant.TenantContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class EquipmentService {

	private final EquipmentRepository equipmentRepository;
	private final EmployeeRepository employeeRepository;
	private final TenantContext tenantContext;
	private final EquipmentMapper equipmentMapper;

	public EquipmentService(
			EquipmentRepository equipmentRepository,
			EmployeeRepository employeeRepository,
			TenantContext tenantContext,
			EquipmentMapper equipmentMapper
	) {
		this.equipmentRepository = equipmentRepository;
		this.employeeRepository = employeeRepository;
		this.tenantContext = tenantContext;
		this.equipmentMapper = equipmentMapper;
	}

	public List<EquipmentResponse> getAll(
			String search,
			String category,
			UUID employeeId,
			EquipmentCondition conditionStatus,
			Boolean active
	) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		List<Equipment> equipmentList = equipmentRepository.findAllByOrganizationIdAndFilters(
				organizationId,
				normalizeSearch(search),
				normalize(category),
				employeeId,
				conditionStatus,
				active
		);
		Map<UUID, Employee> employeesById = loadEmployees(organizationId, equipmentList);
		return equipmentList.stream()
				.map(equipment -> {
					Employee employee = equipment.getEmployeeId() != null
							? employeesById.get(equipment.getEmployeeId())
							: null;
					return equipmentMapper.toResponse(equipment, employee);
				})
				.toList();
	}

	public EquipmentResponse getById(UUID equipmentId) {
		return toResponse(requireEquipmentInCurrentOrganization(equipmentId));
	}

	@Transactional
	public EquipmentResponse create(CreateEquipmentRequest request) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		assertUniqueCode(organizationId, request.code(), null);
		Employee employee = resolveEmployee(organizationId, request.employeeId());

		Equipment equipment = new Equipment();
		equipment.setOrganizationId(organizationId);
		equipmentMapper.applyCreate(equipment, request, employee != null ? employee.getId() : null);

		try {
			return toResponse(equipmentRepository.saveAndFlush(equipment));
		}
		catch (DataIntegrityViolationException ex) {
			throw new BusinessConflictException("Equipment code already exists in organization");
		}
	}

	@Transactional
	public EquipmentResponse update(UUID equipmentId, UpdateEquipmentRequest request) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		Equipment equipment = requireEquipmentInCurrentOrganization(equipmentId);
		assertUniqueCode(organizationId, request.code(), equipmentId);
		Employee employee = resolveEmployee(organizationId, request.employeeId());
		equipmentMapper.applyUpdate(equipment, request, employee != null ? employee.getId() : null);

		try {
			return toResponse(equipmentRepository.save(equipment));
		}
		catch (DataIntegrityViolationException ex) {
			throw new BusinessConflictException("Equipment code already exists in organization");
		}
	}

	@Transactional
	public void deactivate(UUID equipmentId) {
		Equipment equipment = requireEquipmentInCurrentOrganization(equipmentId);
		if (!equipment.isActive()) {
			return;
		}
		equipment.setActive(false);
		equipmentRepository.save(equipment);
	}

	private Equipment requireEquipmentInCurrentOrganization(UUID equipmentId) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		return equipmentRepository.findByIdAndOrganizationId(equipmentId, organizationId)
				.orElseThrow(() -> new ResourceNotFoundException("Equipment", equipmentId));
	}

	private EquipmentResponse toResponse(Equipment equipment) {
		Employee employee = null;
		if (equipment.getEmployeeId() != null) {
			UUID organizationId = tenantContext.getCurrentOrganizationId();
			employee = employeeRepository.findByIdAndOrganizationId(equipment.getEmployeeId(), organizationId).orElse(null);
		}
		return equipmentMapper.toResponse(equipment, employee);
	}

	private Map<UUID, Employee> loadEmployees(UUID organizationId, List<Equipment> equipmentList) {
		List<UUID> employeeIds = equipmentList.stream()
				.map(Equipment::getEmployeeId)
				.filter(id -> id != null)
				.distinct()
				.toList();
		if (employeeIds.isEmpty()) {
			return Map.of();
		}
		Map<UUID, Employee> employeesById = new HashMap<>();
		employeeRepository.findAllByIdInAndOrganizationId(employeeIds, organizationId)
				.forEach(employee -> employeesById.put(employee.getId(), employee));
		return employeesById;
	}

	private Employee resolveEmployee(UUID organizationId, UUID employeeId) {
		if (employeeId == null) {
			return null;
		}
		return employeeRepository.findByIdAndOrganizationId(employeeId, organizationId)
				.orElseThrow(() -> new BusinessConflictException("Employee not found in organization"));
	}

	private void assertUniqueCode(UUID organizationId, String code, UUID excludeEquipmentId) {
		equipmentRepository.findByOrganizationIdAndCode(organizationId, code).ifPresent(existing -> {
			if (excludeEquipmentId == null || !existing.getId().equals(excludeEquipmentId)) {
				throw new BusinessConflictException("Equipment code already exists in organization");
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
