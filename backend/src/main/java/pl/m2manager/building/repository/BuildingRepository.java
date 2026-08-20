package pl.m2manager.building.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.m2manager.building.entity.Building;
import pl.m2manager.building.entity.BuildingStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BuildingRepository extends JpaRepository<Building, UUID> {

	Optional<Building> findByIdAndOrganizationId(UUID id, UUID organizationId);

	Optional<Building> findByOrganizationIdAndCode(UUID organizationId, String code);

	@Query("""
			SELECT b FROM Building b
			WHERE b.organization.id = :organizationId
			  AND (:status IS NULL OR b.status = :status)
			  AND (
			    :search IS NULL OR :search = '' OR
			    LOWER(b.code) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(b.address) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(b.city) LIKE LOWER(CONCAT('%', :search, '%'))
			  )
			ORDER BY b.name ASC
			""")
	List<Building> findAllByOrganizationIdAndFilters(
			@Param("organizationId") UUID organizationId,
			@Param("status") BuildingStatus status,
			@Param("search") String search
	);

	@Query("""
			SELECT b FROM Building b
			WHERE b.organization.id = :organizationId
			  AND b.id IN :ids
			""")
	List<Building> findAllByIdInAndOrganizationId(
			@Param("ids") Collection<UUID> ids,
			@Param("organizationId") UUID organizationId
	);
}
