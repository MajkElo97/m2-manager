package pl.m2manager.scope.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.m2manager.scope.entity.ActivityScope;
import pl.m2manager.scope.entity.ScopePlanningType;
import pl.m2manager.scope.entity.ScopeStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ActivityScopeRepository extends JpaRepository<ActivityScope, UUID> {

	Optional<ActivityScope> findByIdAndOrganizationId(UUID id, UUID organizationId);

	Optional<ActivityScope> findByOrganizationIdAndCode(UUID organizationId, String code);

	@Query("""
			SELECT s FROM ActivityScope s
			WHERE s.organizationId = :organizationId
			  AND (:buildingId IS NULL OR s.buildingId = :buildingId)
			  AND (:activityId IS NULL OR s.activityId = :activityId)
			  AND (:planningType IS NULL OR s.planningType = :planningType)
			  AND (:status IS NULL OR s.status = :status)
			  AND (
			    :search IS NULL OR :search = '' OR
			    LOWER(s.code) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(s.notes) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(s.weekdays) LIKE LOWER(CONCAT('%', :search, '%'))
			  )
			ORDER BY s.code ASC
			""")
	List<ActivityScope> findAllByOrganizationIdAndFilters(
			@Param("organizationId") UUID organizationId,
			@Param("buildingId") UUID buildingId,
			@Param("activityId") UUID activityId,
			@Param("planningType") ScopePlanningType planningType,
			@Param("status") ScopeStatus status,
			@Param("search") String search
	);
}
