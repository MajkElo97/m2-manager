package pl.m2manager.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.m2manager.inventory.entity.Chemical;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChemicalRepository extends JpaRepository<Chemical, UUID> {

	Optional<Chemical> findByIdAndOrganizationId(UUID id, UUID organizationId);

	Optional<Chemical> findByOrganizationIdAndCode(UUID organizationId, String code);

	@Query("""
			SELECT c FROM Chemical c
			WHERE c.organizationId = :organizationId
			  AND (:active IS NULL OR c.active = :active)
			  AND (:category IS NULL OR :category = '' OR LOWER(c.category) = LOWER(:category))
			  AND (
			    :lowStock IS NULL OR :lowStock = FALSE OR
			    (c.minimumStock IS NOT NULL AND c.quantity < c.minimumStock)
			  )
			  AND (
			    :search IS NULL OR :search = '' OR
			    LOWER(c.code) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(c.category) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(c.location) LIKE LOWER(CONCAT('%', :search, '%'))
			  )
			ORDER BY c.code ASC
			""")
	List<Chemical> findAllByOrganizationIdAndFilters(
			@Param("organizationId") UUID organizationId,
			@Param("search") String search,
			@Param("category") String category,
			@Param("active") Boolean active,
			@Param("lowStock") Boolean lowStock
	);
}
