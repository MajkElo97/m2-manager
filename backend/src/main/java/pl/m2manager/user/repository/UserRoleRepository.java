package pl.m2manager.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.m2manager.user.entity.UserRole;
import pl.m2manager.user.entity.UserRoleId;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
}
