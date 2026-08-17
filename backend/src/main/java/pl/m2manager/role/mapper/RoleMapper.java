package pl.m2manager.role.mapper;

import org.springframework.stereotype.Component;
import pl.m2manager.role.dto.RoleResponse;
import pl.m2manager.role.entity.Role;

@Component
public class RoleMapper {

	public RoleResponse toResponse(Role role) {
		return new RoleResponse(
				role.getId(),
				role.getName(),
				role.getDescription(),
				role.isSystemRole(),
				role.isActive()
		);
	}
}
