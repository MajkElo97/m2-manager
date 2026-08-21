package pl.m2manager.activity.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.m2manager.activity.dto.request.CreateActivityRequest;
import pl.m2manager.activity.dto.request.UpdateActivityRequest;
import pl.m2manager.activity.dto.response.ActivityResponse;
import pl.m2manager.activity.entity.Activity;
import pl.m2manager.activity.entity.ActivityPlanningType;
import pl.m2manager.activity.mapper.ActivityMapper;
import pl.m2manager.activity.repository.ActivityRepository;
import pl.m2manager.common.exception.BusinessConflictException;
import pl.m2manager.common.exception.ResourceNotFoundException;
import pl.m2manager.organization.entity.Organization;
import pl.m2manager.organization.repository.OrganizationRepository;
import pl.m2manager.security.auth.OrganizationAccessService;
import pl.m2manager.security.jwt.JwtAuthenticatedPrincipal;
import pl.m2manager.security.jwt.JwtAuthenticationToken;
import pl.m2manager.tenant.TenantContext;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ActivityService {

	private final ActivityRepository activityRepository;
	private final ActivityMapper activityMapper;
	private final TenantContext tenantContext;
	private final OrganizationAccessService organizationAccessService;
	private final OrganizationRepository organizationRepository;

	public ActivityService(
			ActivityRepository activityRepository,
			ActivityMapper activityMapper,
			TenantContext tenantContext,
			OrganizationAccessService organizationAccessService,
			OrganizationRepository organizationRepository
	) {
		this.activityRepository = activityRepository;
		this.activityMapper = activityMapper;
		this.tenantContext = tenantContext;
		this.organizationAccessService = organizationAccessService;
		this.organizationRepository = organizationRepository;
	}

	public List<ActivityResponse> getAll(String search, String category, ActivityPlanningType planningType, Boolean active) {
		RequestContext context = requireRequestContext();
		return activityRepository.findAllVisibleByOrganizationIdAndFilters(
				context.organizationId(),
				normalizeSearch(search),
				normalize(category),
				planningType,
				active
		).stream()
				.map(activity -> activityMapper.toResponse(activity, isManageable(activity, context)))
				.toList();
	}

	public ActivityResponse getById(UUID activityId) {
		RequestContext context = requireRequestContext();
		Activity activity = requireVisibleActivity(activityId, context.organizationId());
		return activityMapper.toResponse(activity, isManageable(activity, context));
	}

	@Transactional
	public ActivityResponse create(CreateActivityRequest request) {
		RequestContext context = requireRequestContext();
		boolean createSystemActivity = Boolean.TRUE.equals(request.system());

		if (createSystemActivity) {
			requireSuperAdmin(context);
			String code = requireCode(request.code(), "System activity code is required");
			assertUniqueCode(code, null);

			Activity activity = new Activity();
			activity.setOrganizationId(null);
			activityMapper.applyCreate(activity, request, code);
			return saveCreatedActivity(activity, context);
		}

		requireBusinessOrganizationContext(context.organizationId());

		String code = resolveOrganizationActivityCode(context.organizationId(), request.code());
		assertUniqueCode(code, null);

		Activity activity = new Activity();
		activity.setOrganizationId(context.organizationId());
		activityMapper.applyCreate(activity, request, code);
		return saveCreatedActivity(activity, context);
	}

	@Transactional
	public ActivityResponse update(UUID activityId, UpdateActivityRequest request) {
		RequestContext context = requireRequestContext();
		Activity activity = requireVisibleActivity(activityId, context.organizationId());
		requireManageAccess(activity, context);

		activityMapper.applyUpdate(activity, request);

		try {
			return activityMapper.toResponse(activityRepository.save(activity), isManageable(activity, context));
		}
		catch (DataIntegrityViolationException ex) {
			throw new BusinessConflictException("Activity code already exists");
		}
	}

	@Transactional
	public void deactivate(UUID activityId) {
		RequestContext context = requireRequestContext();
		Activity activity = requireVisibleActivity(activityId, context.organizationId());
		requireManageAccess(activity, context);
		if (!activity.isActive()) {
			return;
		}
		activity.setActive(false);
		activityRepository.save(activity);
	}

	public Activity requireActiveActivityVisibleToCurrentOrganization(UUID activityId) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		Activity activity = requireActiveActivity(activityId);
		if (!isVisibleToOrganization(activity, organizationId)) {
			throw new ResourceNotFoundException("Activity", activityId);
		}
		return activity;
	}

	public Activity requireActiveActivity(UUID activityId) {
		Activity activity = requireActivity(activityId);
		if (!activity.isActive()) {
			throw new BusinessConflictException("Activity is not active");
		}
		return activity;
	}

	private ActivityResponse saveCreatedActivity(Activity activity, RequestContext context) {
		try {
			return activityMapper.toResponse(
					activityRepository.saveAndFlush(activity),
					isManageable(activity, context)
			);
		}
		catch (DataIntegrityViolationException ex) {
			throw new BusinessConflictException("Activity code already exists");
		}
	}

	private Activity requireVisibleActivity(UUID activityId, UUID organizationId) {
		Activity activity = requireActivity(activityId);
		if (!isVisibleToOrganization(activity, organizationId)) {
			throw new ResourceNotFoundException("Activity", activityId);
		}
		return activity;
	}

	private Activity requireActivity(UUID activityId) {
		return activityRepository.findById(activityId)
				.orElseThrow(() -> new ResourceNotFoundException("Activity", activityId));
	}

	private void requireManageAccess(Activity activity, RequestContext context) {
		if (!isManageable(activity, context)) {
			throw new AccessDeniedException("Activity management denied");
		}
	}

	private void requireSuperAdmin(RequestContext context) {
		if (!organizationAccessService.isSuperAdmin(context.userId())) {
			throw new AccessDeniedException("System activity management denied");
		}
	}

	private boolean isVisibleToOrganization(Activity activity, UUID organizationId) {
		return activity.isSystemActivity() || organizationId.equals(activity.getOrganizationId());
	}

	private boolean isManageable(Activity activity, RequestContext context) {
		if (activity.isSystemActivity()) {
			return organizationAccessService.isSuperAdmin(context.userId());
		}
		if (organizationAccessService.isSystemOrganization(context.organizationId())) {
			return false;
		}
		return context.organizationId().equals(activity.getOrganizationId());
	}

	private void requireBusinessOrganizationContext(UUID organizationId) {
		if (organizationAccessService.isSystemOrganization(organizationId)) {
			throw new AccessDeniedException("Organization activities cannot be created in system organization context");
		}
	}

	private String resolveOrganizationActivityCode(UUID organizationId, String requestedCode) {
		if (requestedCode != null && !requestedCode.isBlank()) {
			String trimmed = requestedCode.trim();
			validateOrganizationActivityCode(trimmed);
			return trimmed;
		}
		return generateOrganizationActivityCode(organizationId);
	}

	private void validateOrganizationActivityCode(String code) {
		if (code.startsWith("CZ") && code.length() >= 3 && code.substring(2).chars().allMatch(Character::isDigit)) {
			throw new BusinessConflictException("Organization activity code cannot use system code format");
		}
		if (!code.startsWith("ORG-")) {
			throw new BusinessConflictException("Organization activity code must start with ORG-");
		}
	}

	private String generateOrganizationActivityCode(UUID organizationId) {
		Organization organization = organizationRepository.findById(organizationId)
				.orElseThrow(() -> new ResourceNotFoundException("Organization", organizationId));
		String prefix = buildOrganizationCodePrefix(organization.getSlug());
		int sequence = (int) activityRepository.countByOrganizationId(organizationId) + 1;

		String candidate;
		do {
			candidate = "ORG-" + prefix + String.format("%03d", sequence++);
		}
		while (activityRepository.findByCode(candidate).isPresent());

		return candidate;
	}

	private String buildOrganizationCodePrefix(String slug) {
		String cleaned = slug.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
		if (cleaned.length() >= 3) {
			return cleaned.substring(0, 3);
		}
		if (!cleaned.isEmpty()) {
			return cleaned;
		}
		return "ORG";
	}

	private String requireCode(String code, String message) {
		if (code == null || code.isBlank()) {
			throw new BusinessConflictException(message);
		}
		return code.trim();
	}

	private void assertUniqueCode(String code, UUID excludeActivityId) {
		activityRepository.findByCode(code).ifPresent(existing -> {
			if (excludeActivityId == null || !existing.getId().equals(excludeActivityId)) {
				throw new BusinessConflictException("Activity code already exists");
			}
		});
	}

	private RequestContext requireRequestContext() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (!(authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) || !authentication.isAuthenticated()) {
			throw new AccessDeniedException("Authentication required");
		}
		JwtAuthenticatedPrincipal principal = jwtAuthenticationToken.getPrincipal();
		return new RequestContext(principal.userId(), tenantContext.getCurrentOrganizationId());
	}

	private String normalizeSearch(String search) {
		if (search == null) {
			return null;
		}
		String trimmed = search.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private String normalize(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private record RequestContext(UUID userId, UUID organizationId) {
	}
}
