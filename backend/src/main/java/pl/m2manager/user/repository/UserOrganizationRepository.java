package pl.m2manager.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.m2manager.user.entity.UserOrganization;
import pl.m2manager.user.entity.UserOrganizationId;

import java.util.List;
import java.util.UUID;

public interface UserOrganizationRepository extends JpaRepository<UserOrganization, UserOrganizationId> {

	boolean existsByIdUserIdAndIdOrganizationId(UUID userId, UUID organizationId);

	@Query("""
			SELECT uo.id.organizationId
			FROM UserOrganization uo
			WHERE uo.id.userId = :userId
			ORDER BY uo.id.organizationId
			""")
	List<UUID> findOrganizationIdsByUserId(@Param("userId") UUID userId);
}
