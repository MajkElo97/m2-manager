package pl.m2manager.manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.m2manager.manager.entity.Manager;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ManagerRepository extends JpaRepository<Manager, UUID> {

	Optional<Manager> findByIdAndOrganizationId(UUID id, UUID organizationId);

	List<Manager> findAllByIdInAndOrganizationId(Collection<UUID> ids, UUID organizationId);

	Optional<Manager> findByOrganizationIdAndCode(UUID organizationId, String code);

	Optional<Manager> findByOrganizationIdAndName(UUID organizationId, String name);

	@Query("""
			SELECT m FROM Manager m
			WHERE m.organizationId = :organizationId
			  AND (:active IS NULL OR m.active = :active)
			  AND (
			    :search IS NULL OR :search = '' OR
			    LOWER(m.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(m.phone) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(m.email) LIKE LOWER(CONCAT('%', :search, '%'))
			  )
			ORDER BY m.name ASC
			""")
	List<Manager> findAllByOrganizationIdAndFilters(
			@Param("organizationId") UUID organizationId,
			@Param("search") String search,
			@Param("active") Boolean active
	);

	@Query(value = "SELECT COUNT(*) FROM supervisors WHERE manager_id = :managerId", nativeQuery = true)
	long countSupervisorsByManagerId(@Param("managerId") UUID managerId);
}
