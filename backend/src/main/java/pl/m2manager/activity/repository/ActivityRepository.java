package pl.m2manager.activity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.m2manager.activity.entity.Activity;
import pl.m2manager.activity.entity.ActivityPlanningType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ActivityRepository extends JpaRepository<Activity, UUID> {

	Optional<Activity> findByCode(String code);

	@Query("""
			SELECT a FROM Activity a
			WHERE (:active IS NULL OR a.active = :active)
			  AND (:category IS NULL OR :category = '' OR a.category = :category)
			  AND (:planningType IS NULL OR a.planningType = :planningType)
			  AND (
			    :search IS NULL OR :search = '' OR
			    LOWER(a.code) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%'))
			  )
			ORDER BY a.code ASC
			""")
	List<Activity> findAllByFilters(
			@Param("search") String search,
			@Param("category") String category,
			@Param("planningType") ActivityPlanningType planningType,
			@Param("active") Boolean active
	);
}
