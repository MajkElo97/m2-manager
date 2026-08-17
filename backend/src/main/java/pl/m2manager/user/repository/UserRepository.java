package pl.m2manager.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.m2manager.user.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

	Optional<User> findByIdAndOrganizationId(UUID id, UUID organizationId);

	List<User> findByOrganizationId(UUID organizationId);

	Optional<User> findByOrganizationIdAndEmail(UUID organizationId, String email);
}
