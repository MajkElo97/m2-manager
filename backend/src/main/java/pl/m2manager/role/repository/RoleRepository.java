package pl.m2manager.role.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.m2manager.role.entity.Role;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

	Optional<Role> findByIdAndOrganizationId(UUID id, UUID organizationId);

	List<Role> findByOrganizationId(UUID organizationId);

	Optional<Role> findByOrganizationIdAndName(UUID organizationId, String name);

	@Query("""
			SELECT r FROM Role r
			JOIN UserRole ur ON ur.id.roleId = r.id AND ur.organizationId = r.organization.id
			WHERE ur.id.userId = :userId AND ur.organizationId = :organizationId
			""")
	List<Role> findRolesByUserIdAndOrganizationId(
			@Param("userId") UUID userId,
			@Param("organizationId") UUID organizationId
	);

	@Query("""
			SELECT COUNT(ur) FROM UserRole ur
			WHERE ur.id.roleId = :roleId AND ur.organizationId = :organizationId
			""")
	long countUsersByRoleIdAndOrganizationId(
			@Param("roleId") UUID roleId,
			@Param("organizationId") UUID organizationId
	);
}
