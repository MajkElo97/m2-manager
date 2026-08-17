package pl.m2manager.role.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.m2manager.role.entity.Role;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

	Optional<Role> findByIdAndOrganizationId(UUID id, UUID organizationId);

	List<Role> findByOrganizationId(UUID organizationId);

	Optional<Role> findByOrganizationIdAndName(UUID organizationId, String name);
}
