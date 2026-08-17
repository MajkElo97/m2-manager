package pl.m2manager.organization.mapper;

import org.springframework.stereotype.Component;
import pl.m2manager.organization.dto.response.OrganizationResponse;
import pl.m2manager.organization.entity.Organization;

@Component
public class OrganizationMapper {

	public OrganizationResponse toResponse(Organization organization) {
		return new OrganizationResponse(
				organization.getId(),
				organization.getName(),
				organization.getSlug(),
				organization.getNip(),
				organization.getEmail(),
				organization.getPhone(),
				organization.isActive(),
				organization.getTimezone(),
				organization.getCreatedAt(),
				organization.getUpdatedAt()
		);
	}
}
