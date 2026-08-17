package pl.m2manager.permission.mapper;

import org.springframework.stereotype.Component;
import pl.m2manager.permission.dto.PermissionResponse;
import pl.m2manager.permission.entity.Permission;

@Component
public class PermissionMapper {

	public PermissionResponse toResponse(Permission permission) {
		return new PermissionResponse(
				permission.getCode(),
				permission.getModule(),
				permission.getAction(),
				permission.getDescription()
		);
	}
}
