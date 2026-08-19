package pl.m2manager.supervisor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.m2manager.supervisor.entity.Supervisor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SupervisorRepository extends JpaRepository<Supervisor, UUID> {

	Optional<Supervisor> findByIdAndOrganizationId(UUID id, UUID organizationId);

	Optional<Supervisor> findByOrganizationIdAndCode(UUID organizationId, String code);

	@Query("""
			SELECT s FROM Supervisor s
			WHERE s.organizationId = :organizationId
			  AND (:managerId IS NULL OR s.managerId = :managerId)
			  AND (:active IS NULL OR s.active = :active)
			  AND (
			    :search IS NULL OR :search = '' OR
			    LOWER(s.code) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(s.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(s.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(s.email) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(s.phone) LIKE LOWER(CONCAT('%', :search, '%'))
			  )
			ORDER BY s.code ASC
			""")
	List<Supervisor> findAllByOrganizationIdAndFilters(
			@Param("organizationId") UUID organizationId,
			@Param("managerId") UUID managerId,
			@Param("active") Boolean active,
			@Param("search") String search
	);
}
