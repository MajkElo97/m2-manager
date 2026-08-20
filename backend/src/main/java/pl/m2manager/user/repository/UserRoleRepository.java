package pl.m2manager.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.m2manager.user.entity.UserRole;
import pl.m2manager.user.entity.UserRoleId;

import java.util.List;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

	@Query("""
			SELECT DISTINCT p.code
			FROM UserRole ur
			JOIN Role r ON r.id = ur.id.roleId AND r.organization.id = ur.organizationId
			JOIN RolePermission rp ON rp.id.roleId = r.id
			JOIN Permission p ON p.id = rp.id.permissionId
			WHERE ur.id.userId = :userId
			  AND ur.organizationId = :organizationId
			  AND r.active = true
			""")
	List<String> findEffectivePermissionCodesByUserIdAndOrganizationId(
			@Param("userId") UUID userId,
			@Param("organizationId") UUID organizationId
	);

	List<UserRole> findByOrganizationIdAndIdUserId(UUID organizationId, UUID userId);

	@Query("""
			SELECT CASE WHEN COUNT(ur) > 0 THEN true ELSE false END
			FROM UserRole ur
			JOIN Role r ON r.id = ur.id.roleId AND r.organization.id = ur.organizationId
			WHERE ur.id.userId = :userId
			  AND r.name = 'SUPER_ADMIN'
			  AND r.systemRole = true
			  AND r.active = true
			""")
	boolean hasSuperAdminRole(@Param("userId") UUID userId);
}
