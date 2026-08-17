package pl.m2manager.organization.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.m2manager.common.exception.ResourceNotFoundException;
import pl.m2manager.organization.dto.request.UpdateOrganizationRequest;
import pl.m2manager.organization.dto.response.OrganizationResponse;
import pl.m2manager.organization.entity.Organization;
import pl.m2manager.organization.mapper.OrganizationMapper;
import pl.m2manager.organization.repository.OrganizationRepository;
import pl.m2manager.tenant.TenantContext;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class OrganizationService {

	private final OrganizationRepository organizationRepository;
	private final TenantContext tenantContext;
	private final OrganizationMapper organizationMapper;

	public OrganizationService(
			OrganizationRepository organizationRepository,
			TenantContext tenantContext,
			OrganizationMapper organizationMapper
	) {
		this.organizationRepository = organizationRepository;
		this.tenantContext = tenantContext;
		this.organizationMapper = organizationMapper;
	}

	public OrganizationResponse getCurrentOrganization() {
		return organizationMapper.toResponse(findCurrentOrganization());
	}

	@Transactional
	public OrganizationResponse updateCurrentOrganization(UpdateOrganizationRequest request) {
		Organization organization = findCurrentOrganization();

		organization.setName(request.name());
		organization.setNip(request.nip());
		organization.setEmail(request.email());
		organization.setPhone(request.phone());
		organization.setTimezone(request.timezone());

		return organizationMapper.toResponse(organizationRepository.save(organization));
	}

	private Organization findCurrentOrganization() {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		return organizationRepository.findById(organizationId)
				.orElseThrow(() -> new ResourceNotFoundException("Organization", organizationId));
	}
}
