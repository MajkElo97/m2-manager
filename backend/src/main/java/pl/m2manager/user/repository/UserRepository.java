package pl.m2manager.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.m2manager.user.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

	Optional<User> findByIdAndOrganizationId(UUID id, UUID organizationId);

	List<User> findByOrganizationId(UUID organizationId);

	Optional<User> findByOrganizationIdAndEmail(UUID organizationId, String email);

	@Query("""
			SELECT DISTINCT u FROM User u
			LEFT JOIN UserRole ur ON ur.id.userId = u.id AND ur.organizationId = u.organization.id
			WHERE u.organization.id = :organizationId
			  AND (:active IS NULL OR u.active = :active)
			  AND (:roleId IS NULL OR ur.id.roleId = :roleId)
			  AND (
			    :search IS NULL OR :search = '' OR
			    LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR
			    LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
			  )
			ORDER BY u.lastName ASC NULLS LAST, u.firstName ASC
			""")
	List<User> findAllByOrganizationIdAndFilters(
			@Param("organizationId") UUID organizationId,
			@Param("search") String search,
			@Param("active") Boolean active,
			@Param("roleId") UUID roleId
	);
}
