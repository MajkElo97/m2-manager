package pl.m2manager.organization.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.m2manager.organization.dto.request.UpdateOrganizationRequest;
import pl.m2manager.organization.dto.response.OrganizationResponse;
import pl.m2manager.organization.service.OrganizationService;

@RestController
@RequestMapping("/api/organization")
public class OrganizationController {

	private final OrganizationService organizationService;

	public OrganizationController(OrganizationService organizationService) {
		this.organizationService = organizationService;
	}

	@GetMapping
	public OrganizationResponse getCurrentOrganization() {
		return organizationService.getCurrentOrganization();
	}

	@PutMapping
	public OrganizationResponse updateCurrentOrganization(
			@Valid @RequestBody UpdateOrganizationRequest request
	) {
		return organizationService.updateCurrentOrganization(request);
	}
}
