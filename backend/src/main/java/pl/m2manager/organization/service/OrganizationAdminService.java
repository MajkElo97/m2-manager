package pl.m2manager.organization.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.m2manager.common.exception.BusinessConflictException;
import pl.m2manager.common.exception.ResourceNotFoundException;
import pl.m2manager.organization.dto.request.CreateOrganizationRequest;
import pl.m2manager.organization.dto.request.UpdateOrganizationAdminRequest;
import pl.m2manager.organization.dto.response.CreateOrganizationResponse;
import pl.m2manager.organization.dto.response.OrganizationDetailResponse;
import pl.m2manager.organization.dto.response.OrganizationListItemResponse;
import pl.m2manager.organization.dto.response.ResetAdminPasswordResponse;
import pl.m2manager.organization.entity.Organization;
import pl.m2manager.organization.repository.OrganizationRepository;
import pl.m2manager.role.entity.Role;
import pl.m2manager.security.auth.AuthContextService;
import pl.m2manager.security.auth.SecurePasswordGenerator;
import pl.m2manager.security.jwt.RefreshTokenRepository;
import pl.m2manager.user.entity.User;
import pl.m2manager.user.entity.UserRole;
import pl.m2manager.user.repository.UserRepository;
import pl.m2manager.user.repository.UserRoleRepository;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class OrganizationAdminService {

	private final OrganizationRepository organizationRepository;
	private final UserRepository userRepository;
	private final UserRoleRepository userRoleRepository;
	private final BusinessRoleProvisioningService businessRoleProvisioningService;
	private final PasswordEncoder passwordEncoder;
	private final RefreshTokenRepository refreshTokenRepository;
	private final Clock clock;

	public OrganizationAdminService(
			OrganizationRepository organizationRepository,
			UserRepository userRepository,
			UserRoleRepository userRoleRepository,
			BusinessRoleProvisioningService businessRoleProvisioningService,
			PasswordEncoder passwordEncoder,
			RefreshTokenRepository refreshTokenRepository,
			Clock clock
	) {
		this.organizationRepository = organizationRepository;
		this.userRepository = userRepository;
		this.userRoleRepository = userRoleRepository;
		this.businessRoleProvisioningService = businessRoleProvisioningService;
		this.passwordEncoder = passwordEncoder;
		this.refreshTokenRepository = refreshTokenRepository;
		this.clock = clock;
	}

	public List<OrganizationListItemResponse> listOrganizations(String search, Boolean active) {
		return organizationRepository.findBusinessOrganizations(normalizeSearch(search), active).stream()
				.map(this::toListItem)
				.toList();
	}

	public OrganizationDetailResponse getOrganization(UUID organizationId) {
		return toDetail(requireBusinessOrganization(organizationId));
	}

	@Transactional
	public CreateOrganizationResponse createOrganization(CreateOrganizationRequest request) {
		String name = request.name().trim();
		String slug = request.slug().trim().toLowerCase();
		String adminEmail = request.adminEmail().trim().toLowerCase();

		assertUniqueName(name, null);
		assertUniqueSlug(slug, null);
		assertUniqueEmail(adminEmail);

		Organization organization = new Organization();
		organization.setName(name);
		organization.setSlug(slug);
		organization.setTimezone("Europe/Warsaw");
		organization.setActive(true);
		organization.setSystemOrganization(false);

		try {
			organization = organizationRepository.saveAndFlush(organization);
		}
		catch (DataIntegrityViolationException ex) {
			throw conflictFromConstraint(ex);
		}

		Map<String, Role> roles = businessRoleProvisioningService.provisionBusinessRoles(organization);
		Role adminRole = roles.get("ADMIN");

		String temporaryPassword = SecurePasswordGenerator.generateTemporaryPassword();
		User adminUser = new User();
		adminUser.setOrganization(organization);
		adminUser.setEmail(adminEmail);
		adminUser.setFirstName("Administrator");
		adminUser.setLastName(organization.getName());
		adminUser.setPasswordHash(passwordEncoder.encode(temporaryPassword));
		adminUser.setActive(true);
		adminUser.setMustChangePassword(true);

		try {
			adminUser = userRepository.saveAndFlush(adminUser);
		}
		catch (DataIntegrityViolationException ex) {
			throw new BusinessConflictException("Użytkownik o takim loginie już istnieje.");
		}

		userRoleRepository.save(new UserRole(organization.getId(), adminUser.getId(), adminRole.getId()));

		return new CreateOrganizationResponse(
				organization.getId(),
				organization.getName(),
				organization.getSlug(),
				adminUser.getEmail(),
				temporaryPassword
		);
	}

	@Transactional
	public OrganizationDetailResponse updateOrganization(UUID organizationId, UpdateOrganizationAdminRequest request) {
		Organization organization = requireBusinessOrganization(organizationId);
		String name = request.name().trim();
		String slug = request.slug().trim().toLowerCase();

		assertUniqueName(name, organizationId);
		assertUniqueSlug(slug, organizationId);

		organization.setName(name);
		organization.setSlug(slug);

		try {
			return toDetail(organizationRepository.save(organization));
		}
		catch (DataIntegrityViolationException ex) {
			throw conflictFromConstraint(ex);
		}
	}

	@Transactional
	public OrganizationDetailResponse deactivateOrganization(UUID organizationId) {
		Organization organization = requireBusinessOrganization(organizationId);
		if (!organization.isActive()) {
			return toDetail(organization);
		}
		organization.setActive(false);
		return toDetail(organizationRepository.save(organization));
	}

	@Transactional
	public ResetAdminPasswordResponse resetAdminPassword(UUID organizationId) {
		Organization organization = requireBusinessOrganization(organizationId);
		User adminUser = findPrimaryAdmin(organization.getId());

		String temporaryPassword = SecurePasswordGenerator.generateTemporaryPassword();
		adminUser.setPasswordHash(passwordEncoder.encode(temporaryPassword));
		adminUser.setMustChangePassword(true);
		userRepository.save(adminUser);
		refreshTokenRepository.revokeAllActiveByUserId(adminUser.getId(), clock.instant());

		return new ResetAdminPasswordResponse(adminUser.getEmail(), temporaryPassword);
	}

	private Organization requireBusinessOrganization(UUID organizationId) {
		Organization organization = organizationRepository.findById(organizationId)
				.orElseThrow(() -> new ResourceNotFoundException("Organization", organizationId));
		if (organization.isSystemOrganization()) {
			throw new ResourceNotFoundException("Organization", organizationId);
		}
		return organization;
	}

	private User findPrimaryAdmin(UUID organizationId) {
		List<User> admins = userRepository.findOrganizationAdmins(organizationId);
		if (admins.isEmpty()) {
			throw new ResourceNotFoundException("Organization admin not found");
		}
		return admins.getFirst();
	}

	private OrganizationListItemResponse toListItem(Organization organization) {
		User admin = userRepository.findOrganizationAdmins(organization.getId()).stream().findFirst().orElse(null);
		return new OrganizationListItemResponse(
				organization.getId(),
				organization.getName(),
				organization.getSlug(),
				admin != null ? AuthContextService.resolveDisplayName(admin) : "—",
				admin != null ? admin.getEmail() : "—",
				organization.isActive(),
				organization.getCreatedAt()
		);
	}

	private OrganizationDetailResponse toDetail(Organization organization) {
		User admin = userRepository.findOrganizationAdmins(organization.getId()).stream().findFirst().orElse(null);
		return new OrganizationDetailResponse(
				organization.getId(),
				organization.getName(),
				organization.getSlug(),
				admin != null ? AuthContextService.resolveDisplayName(admin) : null,
				admin != null ? admin.getEmail() : null,
				admin != null ? admin.getId() : null,
				organization.isActive(),
				organization.getCreatedAt(),
				organization.getUpdatedAt()
		);
	}

	private void assertUniqueName(String name, UUID excludeId) {
		organizationRepository.findBySystemOrganizationFalseOrderByNameAsc().stream()
				.filter(organization -> organization.getName().equalsIgnoreCase(name))
				.filter(organization -> excludeId == null || !organization.getId().equals(excludeId))
				.findFirst()
				.ifPresent(ignored -> {
					throw new BusinessConflictException("Organizacja o takiej nazwie już istnieje.");
				});
	}

	private void assertUniqueSlug(String slug, UUID excludeId) {
		organizationRepository.findBySlug(slug).ifPresent(existing -> {
			if (excludeId == null || !existing.getId().equals(excludeId)) {
				throw new BusinessConflictException("Organizacja o takim slug już istnieje.");
			}
		});
	}

	private void assertUniqueEmail(String email) {
		if (userRepository.existsByEmail(email)) {
			throw new BusinessConflictException("Użytkownik o takim loginie już istnieje.");
		}
	}

	private BusinessConflictException conflictFromConstraint(DataIntegrityViolationException ex) {
		String message = ex.getMostSpecificCause().getMessage();
		if (message != null) {
			if (message.contains("uq_organizations_slug")) {
				return new BusinessConflictException("Organizacja o takim slug już istnieje.");
			}
			if (message.contains("uq_organizations_name")) {
				return new BusinessConflictException("Organizacja o takiej nazwie już istnieje.");
			}
			if (message.contains("uq_users_email")) {
				return new BusinessConflictException("Użytkownik o takim loginie już istnieje.");
			}
		}
		return new BusinessConflictException("Organization conflict");
	}

	private String normalizeSearch(String search) {
		if (search == null) {
			return null;
		}
		String trimmed = search.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
