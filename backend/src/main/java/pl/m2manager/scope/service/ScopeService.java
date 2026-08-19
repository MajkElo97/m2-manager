package pl.m2manager.scope.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.m2manager.activity.service.ActivityService;
import pl.m2manager.building.repository.BuildingRepository;
import pl.m2manager.common.exception.BusinessConflictException;
import pl.m2manager.common.exception.ResourceNotFoundException;
import pl.m2manager.scope.dto.request.CreateScopeRequest;
import pl.m2manager.scope.dto.request.UpdateScopeRequest;
import pl.m2manager.scope.dto.response.ScopeResponse;
import pl.m2manager.scope.entity.ActivityScope;
import pl.m2manager.scope.entity.ScopePlanningType;
import pl.m2manager.scope.entity.ScopeStatus;
import pl.m2manager.scope.mapper.ScopeMapper;
import pl.m2manager.scope.repository.ActivityScopeRepository;
import pl.m2manager.tenant.TenantContext;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ScopeService {

	private final ActivityScopeRepository scopeRepository;
	private final BuildingRepository buildingRepository;
	private final ActivityService activityService;
	private final TenantContext tenantContext;
	private final ScopeMapper scopeMapper;

	public ScopeService(
			ActivityScopeRepository scopeRepository,
			BuildingRepository buildingRepository,
			ActivityService activityService,
			TenantContext tenantContext,
			ScopeMapper scopeMapper
	) {
		this.scopeRepository = scopeRepository;
		this.buildingRepository = buildingRepository;
		this.activityService = activityService;
		this.tenantContext = tenantContext;
		this.scopeMapper = scopeMapper;
	}

	public List<ScopeResponse> getAll(
			UUID buildingId,
			UUID activityId,
			ScopePlanningType planningType,
			ScopeStatus status,
			String search
	) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		return scopeRepository.findAllByOrganizationIdAndFilters(
				organizationId,
				buildingId,
				activityId,
				planningType,
				status,
				normalizeSearch(search)
		).stream().map(scopeMapper::toResponse).toList();
	}

	public ScopeResponse getById(UUID scopeId) {
		return scopeMapper.toResponse(requireScopeInCurrentOrganization(scopeId));
	}

	@Transactional
	public ScopeResponse create(CreateScopeRequest request) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		requireBuildingInCurrentOrganization(request.buildingId(), organizationId);
		activityService.requireActiveActivity(request.activityId());
		assertUniqueCode(organizationId, request.code(), null);

		ActivityScope scope = new ActivityScope();
		scope.setOrganizationId(organizationId);
		scopeMapper.applyCreate(scope, request);

		try {
			return scopeMapper.toResponse(scopeRepository.saveAndFlush(scope));
		}
		catch (DataIntegrityViolationException ex) {
			throw new BusinessConflictException("Scope code already exists in organization");
		}
	}

	@Transactional
	public ScopeResponse update(UUID scopeId, UpdateScopeRequest request) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		ActivityScope scope = requireScopeInCurrentOrganization(scopeId);
		requireBuildingInCurrentOrganization(request.buildingId(), organizationId);
		activityService.requireActiveActivity(request.activityId());
		assertUniqueCode(organizationId, request.code(), scopeId);
		scopeMapper.applyUpdate(scope, request);

		try {
			return scopeMapper.toResponse(scopeRepository.save(scope));
		}
		catch (DataIntegrityViolationException ex) {
			throw new BusinessConflictException("Scope code already exists in organization");
		}
	}

	@Transactional
	public void deactivate(UUID scopeId) {
		ActivityScope scope = requireScopeInCurrentOrganization(scopeId);
		if (scope.getStatus() == ScopeStatus.INACTIVE) {
			return;
		}
		scope.setStatus(ScopeStatus.INACTIVE);
		scopeRepository.save(scope);
	}

	private ActivityScope requireScopeInCurrentOrganization(UUID scopeId) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		return scopeRepository.findByIdAndOrganizationId(scopeId, organizationId)
				.orElseThrow(() -> new ResourceNotFoundException("Scope", scopeId));
	}

	private void requireBuildingInCurrentOrganization(UUID buildingId, UUID organizationId) {
		buildingRepository.findByIdAndOrganizationId(buildingId, organizationId)
				.orElseThrow(() -> new ResourceNotFoundException("Building", buildingId));
	}

	private void assertUniqueCode(UUID organizationId, String code, UUID excludeScopeId) {
		scopeRepository.findByOrganizationIdAndCode(organizationId, code).ifPresent(existing -> {
			if (excludeScopeId == null || !existing.getId().equals(excludeScopeId)) {
				throw new BusinessConflictException("Scope code already exists in organization");
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
