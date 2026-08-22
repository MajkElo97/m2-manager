package pl.m2manager.user.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.m2manager.common.exception.BusinessConflictException;
import pl.m2manager.common.exception.ResourceNotFoundException;
import pl.m2manager.employee.entity.Employee;
import pl.m2manager.employee.repository.EmployeeRepository;
import pl.m2manager.organization.entity.Organization;
import pl.m2manager.organization.repository.OrganizationRepository;
import pl.m2manager.role.entity.Role;
import pl.m2manager.role.repository.RoleRepository;
import pl.m2manager.tenant.TenantContext;
import pl.m2manager.user.dto.CreateUserRequest;
import pl.m2manager.user.dto.UpdateUserRequest;
import pl.m2manager.user.dto.UserResponse;
import pl.m2manager.user.entity.User;
import pl.m2manager.user.entity.UserRole;
import pl.m2manager.user.entity.UserRoleId;
import pl.m2manager.user.mapper.UserMapper;
import pl.m2manager.user.repository.UserRepository;
import pl.m2manager.user.repository.UserRoleRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserService {

	private final UserRepository userRepository;
	private final UserRoleRepository userRoleRepository;
	private final RoleRepository roleRepository;
	private final EmployeeRepository employeeRepository;
	private final OrganizationRepository organizationRepository;
	private final PasswordEncoder passwordEncoder;
	private final TenantContext tenantContext;
	private final UserMapper userMapper;

	public UserService(
			UserRepository userRepository,
			UserRoleRepository userRoleRepository,
			RoleRepository roleRepository,
			EmployeeRepository employeeRepository,
			OrganizationRepository organizationRepository,
			PasswordEncoder passwordEncoder,
			TenantContext tenantContext,
			UserMapper userMapper
	) {
		this.userRepository = userRepository;
		this.userRoleRepository = userRoleRepository;
		this.roleRepository = roleRepository;
		this.employeeRepository = employeeRepository;
		this.organizationRepository = organizationRepository;
		this.passwordEncoder = passwordEncoder;
		this.tenantContext = tenantContext;
		this.userMapper = userMapper;
	}

	public List<UserResponse> getAll(String search, Boolean active, UUID roleId) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		return userRepository.findAllByOrganizationIdAndFilters(
				organizationId,
				normalizeSearch(search),
				active,
				roleId
		).stream().map(this::toResponse).toList();
	}

	public UserResponse getById(UUID userId) {
		return toResponse(requireUserInCurrentOrganization(userId));
	}

	@Transactional
	public UserResponse create(CreateUserRequest request) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		assertUniqueEmail(organizationId, request.email(), null);

		Organization organization = organizationRepository.findById(organizationId)
				.orElseThrow(() -> new ResourceNotFoundException("Organization", organizationId));

		List<Role> roles = resolveRoles(organizationId, request.roleIds());
		Employee employee = resolveEmployee(organizationId, request.employeeId());

		User user = new User();
		user.setOrganization(organization);
		user.setFirstName(request.firstName().trim());
		user.setLastName(request.lastName().trim());
		user.setEmail(request.email().trim().toLowerCase());
		user.setPasswordHash(passwordEncoder.encode(request.password()));
		user.setEmployee(employee);
		user.setActive(true);

		try {
			User saved = userRepository.saveAndFlush(user);
			replaceUserRoles(saved.getId(), organizationId, roles);
			return toResponse(saved);
		}
		catch (DataIntegrityViolationException ex) {
			throw new BusinessConflictException("Użytkownik o takim loginie już istnieje.");
		}
	}

	@Transactional
	public UserResponse update(UUID userId, UpdateUserRequest request) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		User user = requireUserInCurrentOrganization(userId);
		assertUniqueEmail(organizationId, request.email(), userId);

		List<Role> roles = resolveRoles(organizationId, request.roleIds());
		Employee employee = resolveEmployee(organizationId, request.employeeId());

		user.setFirstName(request.firstName().trim());
		user.setLastName(request.lastName().trim());
		user.setEmail(request.email().trim().toLowerCase());
		if (request.password() != null && !request.password().isBlank()) {
			user.setPasswordHash(passwordEncoder.encode(request.password()));
		}
		user.setEmployee(employee);
		user.setActive(request.active());

		replaceUserRoles(userId, organizationId, roles);

		try {
			return toResponse(userRepository.save(user));
		}
		catch (DataIntegrityViolationException ex) {
			throw new BusinessConflictException("Użytkownik o takim loginie już istnieje.");
		}
	}

	@Transactional
	public void deactivate(UUID userId) {
		User user = requireUserInCurrentOrganization(userId);
		if (!user.isActive()) {
			return;
		}
		user.setActive(false);
		userRepository.save(user);
	}

	private User requireUserInCurrentOrganization(UUID userId) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		return userRepository.findByIdAndOrganizationId(userId, organizationId)
				.orElseThrow(() -> new ResourceNotFoundException("User", userId));
	}

	private UserResponse toResponse(User user) {
		UUID organizationId = user.getOrganization().getId();
		List<Role> roles = roleRepository.findRolesByUserIdAndOrganizationId(user.getId(), organizationId);
		return userMapper.toResponse(user, roles);
	}

	private void replaceUserRoles(UUID userId, UUID organizationId, List<Role> roles) {
		Set<UUID> targetRoleIds = new HashSet<>();
		for (Role role : roles) {
			targetRoleIds.add(role.getId());
		}

		List<UserRole> existingAssignments = userRoleRepository.findByOrganizationIdAndIdUserId(
				organizationId,
				userId
		);

		for (UserRole assignment : existingAssignments) {
			if (!targetRoleIds.contains(assignment.getId().getRoleId())) {
				userRoleRepository.deleteById(assignment.getId());
			}
		}

		for (Role role : roles) {
			UserRoleId id = new UserRoleId(userId, role.getId());
			if (!userRoleRepository.existsById(id)) {
				userRoleRepository.save(new UserRole(organizationId, userId, role.getId()));
			}
		}
	}

	private List<Role> resolveRoles(UUID organizationId, List<UUID> roleIds) {
		if (roleIds == null || roleIds.isEmpty()) {
			throw new BusinessConflictException("At least one role is required");
		}
		return roleIds.stream()
				.distinct()
				.map(roleId -> roleRepository.findByIdAndOrganizationId(roleId, organizationId)
						.orElseThrow(() -> new BusinessConflictException("Role not found in organization")))
				.toList();
	}

	private Employee resolveEmployee(UUID organizationId, UUID employeeId) {
		if (employeeId == null) {
			return null;
		}
		return employeeRepository.findByIdAndOrganizationId(employeeId, organizationId)
				.orElseThrow(() -> new BusinessConflictException("Employee not found in organization"));
	}

	private void assertUniqueEmail(UUID organizationId, String email, UUID excludeUserId) {
		String normalizedEmail = email.trim().toLowerCase();
		userRepository.findByEmail(normalizedEmail).ifPresent(existing -> {
			if (excludeUserId == null || !existing.getId().equals(excludeUserId)) {
				throw new BusinessConflictException("Użytkownik o takim loginie już istnieje.");
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
