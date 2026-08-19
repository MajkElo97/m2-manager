package pl.m2manager.fleet.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.m2manager.common.exception.BusinessConflictException;
import pl.m2manager.common.exception.ResourceNotFoundException;
import pl.m2manager.employee.entity.Employee;
import pl.m2manager.employee.repository.EmployeeRepository;
import pl.m2manager.fleet.dto.request.CreateVehicleRequest;
import pl.m2manager.fleet.dto.request.UpdateVehicleRequest;
import pl.m2manager.fleet.dto.response.VehicleResponse;
import pl.m2manager.fleet.entity.Vehicle;
import pl.m2manager.fleet.entity.VehicleStatus;
import pl.m2manager.fleet.entity.VehicleType;
import pl.m2manager.fleet.mapper.VehicleMapper;
import pl.m2manager.fleet.repository.VehicleRepository;
import pl.m2manager.tenant.TenantContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class VehicleService {

	private final VehicleRepository vehicleRepository;
	private final EmployeeRepository employeeRepository;
	private final TenantContext tenantContext;
	private final VehicleMapper vehicleMapper;

	public VehicleService(
			VehicleRepository vehicleRepository,
			EmployeeRepository employeeRepository,
			TenantContext tenantContext,
			VehicleMapper vehicleMapper
	) {
		this.vehicleRepository = vehicleRepository;
		this.employeeRepository = employeeRepository;
		this.tenantContext = tenantContext;
		this.vehicleMapper = vehicleMapper;
	}

	public List<VehicleResponse> getAll(
			String search,
			VehicleStatus status,
			UUID employeeId,
			VehicleType vehicleType
	) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		List<Vehicle> vehicles = vehicleRepository.findAllByOrganizationIdAndFilters(
				organizationId,
				normalizeSearch(search),
				status,
				employeeId,
				vehicleType
		);
		Map<UUID, Employee> employeesById = loadEmployees(organizationId, vehicles);
		return vehicles.stream()
				.map(vehicle -> {
					Employee employee = vehicle.getEmployeeId() != null
							? employeesById.get(vehicle.getEmployeeId())
							: null;
					return vehicleMapper.toResponse(vehicle, employee);
				})
				.toList();
	}

	public VehicleResponse getById(UUID vehicleId) {
		Vehicle vehicle = requireVehicleInCurrentOrganization(vehicleId);
		return toResponse(vehicle);
	}

	@Transactional
	public VehicleResponse create(CreateVehicleRequest request) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		assertUniqueCode(organizationId, request.code(), null);
		assertUniqueRegistrationNumber(organizationId, request.registrationNumber(), null);
		Employee employee = resolveEmployee(organizationId, request.employeeId());

		Vehicle vehicle = new Vehicle();
		vehicle.setOrganizationId(organizationId);
		vehicleMapper.applyCreate(vehicle, request, employee != null ? employee.getId() : null);

		try {
			return toResponse(vehicleRepository.saveAndFlush(vehicle));
		}
		catch (DataIntegrityViolationException ex) {
			throw conflictFromConstraint(ex);
		}
	}

	@Transactional
	public VehicleResponse update(UUID vehicleId, UpdateVehicleRequest request) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		Vehicle vehicle = requireVehicleInCurrentOrganization(vehicleId);
		assertUniqueCode(organizationId, request.code(), vehicleId);
		assertUniqueRegistrationNumber(organizationId, request.registrationNumber(), vehicleId);
		Employee employee = resolveEmployee(organizationId, request.employeeId());
		vehicleMapper.applyUpdate(vehicle, request, employee != null ? employee.getId() : null);

		try {
			return toResponse(vehicleRepository.save(vehicle));
		}
		catch (DataIntegrityViolationException ex) {
			throw conflictFromConstraint(ex);
		}
	}

	@Transactional
	public void deactivate(UUID vehicleId) {
		Vehicle vehicle = requireVehicleInCurrentOrganization(vehicleId);
		if (vehicle.getStatus() == VehicleStatus.INACTIVE) {
			return;
		}
		vehicle.setStatus(VehicleStatus.INACTIVE);
		vehicleRepository.save(vehicle);
	}

	private Vehicle requireVehicleInCurrentOrganization(UUID vehicleId) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		return vehicleRepository.findByIdAndOrganizationId(vehicleId, organizationId)
				.orElseThrow(() -> new ResourceNotFoundException("Vehicle", vehicleId));
	}

	private VehicleResponse toResponse(Vehicle vehicle) {
		Employee employee = null;
		if (vehicle.getEmployeeId() != null) {
			UUID organizationId = tenantContext.getCurrentOrganizationId();
			employee = employeeRepository.findByIdAndOrganizationId(vehicle.getEmployeeId(), organizationId).orElse(null);
		}
		return vehicleMapper.toResponse(vehicle, employee);
	}

	private Map<UUID, Employee> loadEmployees(UUID organizationId, List<Vehicle> vehicles) {
		List<UUID> employeeIds = vehicles.stream()
				.map(Vehicle::getEmployeeId)
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

	private void assertUniqueCode(UUID organizationId, String code, UUID excludeVehicleId) {
		vehicleRepository.findByOrganizationIdAndCode(organizationId, code).ifPresent(existing -> {
			if (excludeVehicleId == null || !existing.getId().equals(excludeVehicleId)) {
				throw new BusinessConflictException("Vehicle code already exists in organization");
			}
		});
	}

	private void assertUniqueRegistrationNumber(UUID organizationId, String registrationNumber, UUID excludeVehicleId) {
		vehicleRepository.findByOrganizationIdAndRegistrationNumber(organizationId, registrationNumber).ifPresent(existing -> {
			if (excludeVehicleId == null || !existing.getId().equals(excludeVehicleId)) {
				throw new BusinessConflictException("Vehicle registration number already exists in organization");
			}
		});
	}

	private BusinessConflictException conflictFromConstraint(DataIntegrityViolationException ex) {
		String message = ex.getMostSpecificCause().getMessage();
		if (message != null && message.contains("uq_vehicles_organization_registration")) {
			return new BusinessConflictException("Vehicle registration number already exists in organization");
		}
		return new BusinessConflictException("Vehicle code already exists in organization");
	}

	private String normalizeSearch(String search) {
		if (search == null) {
			return null;
		}
		String trimmed = search.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
