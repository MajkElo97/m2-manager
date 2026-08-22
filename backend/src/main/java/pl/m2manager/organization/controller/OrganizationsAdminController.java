package pl.m2manager.organization.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
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
import pl.m2manager.organization.dto.request.CreateOrganizationRequest;
import pl.m2manager.organization.dto.request.UpdateOrganizationAdminRequest;
import pl.m2manager.organization.dto.response.CreateOrganizationResponse;
import pl.m2manager.organization.dto.response.OrganizationDetailResponse;
import pl.m2manager.organization.dto.response.OrganizationListItemResponse;
import pl.m2manager.organization.dto.response.ResetAdminPasswordResponse;
import pl.m2manager.organization.service.OrganizationAdminService;
import pl.m2manager.security.auth.SuperAdminAuthorization;
import pl.m2manager.security.jwt.JwtAuthenticatedPrincipal;
import pl.m2manager.security.jwt.JwtAuthenticationToken;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationsAdminController {

	private final OrganizationAdminService organizationAdminService;
	private final SuperAdminAuthorization superAdminAuthorization;

	public OrganizationsAdminController(
			OrganizationAdminService organizationAdminService,
			SuperAdminAuthorization superAdminAuthorization
	) {
		this.organizationAdminService = organizationAdminService;
		this.superAdminAuthorization = superAdminAuthorization;
	}

	@GetMapping
	public List<OrganizationListItemResponse> listOrganizations(
			@RequestParam(required = false) String search,
			@RequestParam(required = false) Boolean active
	) {
		superAdminAuthorization.requireSuperAdmin(requirePrincipal());
		return organizationAdminService.listOrganizations(search, active);
	}

	@GetMapping("/{id}")
	public OrganizationDetailResponse getOrganization(@PathVariable UUID id) {
		superAdminAuthorization.requireSuperAdmin(requirePrincipal());
		return organizationAdminService.getOrganization(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CreateOrganizationResponse createOrganization(@Valid @RequestBody CreateOrganizationRequest request) {
		superAdminAuthorization.requireSuperAdmin(requirePrincipal());
		return organizationAdminService.createOrganization(request);
	}

	@PutMapping("/{id}")
	public OrganizationDetailResponse updateOrganization(
			@PathVariable UUID id,
			@Valid @RequestBody UpdateOrganizationAdminRequest request
	) {
		superAdminAuthorization.requireSuperAdmin(requirePrincipal());
		return organizationAdminService.updateOrganization(id, request);
	}

	@DeleteMapping("/{id}")
	public OrganizationDetailResponse deactivateOrganization(@PathVariable UUID id) {
		superAdminAuthorization.requireSuperAdmin(requirePrincipal());
		return organizationAdminService.deactivateOrganization(id);
	}

	@PostMapping("/{id}/reset-admin-password")
	public ResetAdminPasswordResponse resetAdminPassword(@PathVariable UUID id) {
		superAdminAuthorization.requireSuperAdmin(requirePrincipal());
		return organizationAdminService.resetAdminPassword(id);
	}

	private JwtAuthenticatedPrincipal requirePrincipal() {
		var authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
			return jwtAuthenticationToken.getPrincipal();
		}
		throw new IllegalStateException("Authenticated JWT principal required");
	}
}
