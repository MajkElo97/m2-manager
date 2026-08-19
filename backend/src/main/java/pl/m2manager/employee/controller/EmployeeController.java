package pl.m2manager.employee.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.m2manager.employee.dto.request.CreateEmployeeRequest;
import pl.m2manager.employee.dto.request.UpdateEmployeeRequest;
import pl.m2manager.employee.dto.response.EmployeeResponse;
import pl.m2manager.employee.entity.EmployeeRole;
import pl.m2manager.employee.entity.EmploymentType;
import pl.m2manager.employee.service.EmployeeService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

	private final EmployeeService employeeService;

	public EmployeeController(EmployeeService employeeService) {
		this.employeeService = employeeService;
	}

	@GetMapping
	@PreAuthorize("@authorizationService.hasPermission('EMPLOYEES_VIEW')")
	public List<EmployeeResponse> list(
			@RequestParam(required = false) String search,
			@RequestParam(required = false) String position,
			@RequestParam(required = false) EmployeeRole role,
			@RequestParam(required = false) EmploymentType employmentType,
			@RequestParam(required = false) Boolean active
	) {
		return employeeService.getAll(search, position, role, employmentType, active);
	}

	@GetMapping("/{id}")
	@PreAuthorize("@authorizationService.hasPermission('EMPLOYEES_VIEW')")
	public EmployeeResponse getById(@PathVariable UUID id) {
		return employeeService.getById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("@authorizationService.hasPermission('EMPLOYEES_CREATE')")
	public EmployeeResponse create(@Valid @RequestBody CreateEmployeeRequest request) {
		return employeeService.create(request);
	}

	@PutMapping("/{id}")
	@PreAuthorize("@authorizationService.hasPermission('EMPLOYEES_EDIT')")
	public EmployeeResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateEmployeeRequest request) {
		return employeeService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("@authorizationService.hasPermission('EMPLOYEES_DELETE')")
	public void deactivate(@PathVariable UUID id) {
		employeeService.deactivate(id);
	}
}
