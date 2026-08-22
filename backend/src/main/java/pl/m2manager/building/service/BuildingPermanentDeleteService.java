package pl.m2manager.building.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.m2manager.building.entity.Building;
import pl.m2manager.building.entity.BuildingStatus;
import pl.m2manager.building.repository.BuildingRepository;
import pl.m2manager.common.exception.BusinessConflictException;
import pl.m2manager.common.exception.ResourceNotFoundException;
import pl.m2manager.security.auth.OrganizationAccessService;
import pl.m2manager.security.auth.SuperAdminAuthorization;
import pl.m2manager.tenant.TenantContext;

import java.util.UUID;

@Service
public class BuildingPermanentDeleteService {

	private static final Logger log = LoggerFactory.getLogger(BuildingPermanentDeleteService.class);

	private final BuildingRepository buildingRepository;
	private final BuildingDependencyChecker buildingDependencyChecker;
	private final TenantContext tenantContext;
	private final SuperAdminAuthorization superAdminAuthorization;
	private final OrganizationAccessService organizationAccessService;

	public BuildingPermanentDeleteService(
			BuildingRepository buildingRepository,
			BuildingDependencyChecker buildingDependencyChecker,
			TenantContext tenantContext,
			SuperAdminAuthorization superAdminAuthorization,
			OrganizationAccessService organizationAccessService
	) {
		this.buildingRepository = buildingRepository;
		this.buildingDependencyChecker = buildingDependencyChecker;
		this.tenantContext = tenantContext;
		this.superAdminAuthorization = superAdminAuthorization;
		this.organizationAccessService = organizationAccessService;
	}

	@Transactional
	public void permanentDelete(UUID buildingId, UUID userId) {
		superAdminAuthorization.requireSuperAdmin(userId);

		UUID organizationId = tenantContext.getCurrentOrganizationId();
		if (organizationAccessService.isSystemOrganization(organizationId)) {
			throw new AccessDeniedException("Permanent delete requires business organization context");
		}

		Building building = buildingRepository.findByIdAndOrganizationId(buildingId, organizationId)
				.orElseThrow(() -> new ResourceNotFoundException("Building", buildingId));

		if (building.getStatus() != BuildingStatus.INACTIVE) {
			throw new BusinessConflictException(
					"Aktywnego budynku nie można usunąć na stałe. Najpierw go dezaktywuj."
			);
		}

		var dependencies = buildingDependencyChecker.countDependencies(organizationId, buildingId);
		if (dependencies.hasBlockingDependencies()) {
			throw new BusinessConflictException(dependencies.formatConflictMessage());
		}

		buildingRepository.delete(building);

		log.info(
				"Permanent delete executed: userId={}, organizationId={}, entityType=Building, entityId={}, buildingCode={}",
				userId,
				organizationId,
				buildingId,
				building.getCode()
		);
	}
}
