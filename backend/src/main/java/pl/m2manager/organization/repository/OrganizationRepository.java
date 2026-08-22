package pl.m2manager.organization.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.m2manager.organization.entity.Organization;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

	Optional<Organization> findBySlug(String slug);

	boolean existsBySlug(String slug);

	boolean existsByName(String name);

	List<Organization> findBySystemOrganizationFalseOrderByNameAsc();

	@Query("""
			SELECT o FROM Organization o
			WHERE o.systemOrganization = false
			  AND (:active IS NULL OR o.active = :active)
			  AND (
			    :search IS NULL OR :search = '' OR
			    LOWER(o.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(o.slug) LIKE LOWER(CONCAT('%', :search, '%'))
			  )
			ORDER BY o.name ASC
			""")
	List<Organization> findBusinessOrganizations(
			@Param("search") String search,
			@Param("active") Boolean active
	);
}
