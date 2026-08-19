package pl.m2manager.user.mapper;

import org.springframework.stereotype.Component;
import pl.m2manager.role.entity.Role;
import pl.m2manager.user.dto.UserResponse;
import pl.m2manager.user.dto.UserRoleSummary;
import pl.m2manager.user.entity.User;

import java.util.List;

@Component
public class UserMapper {

	public UserResponse toResponse(User user, List<Role> roles) {
		return new UserResponse(
				user.getId(),
				user.getFirstName(),
				user.getLastName(),
				user.getEmail(),
				user.isActive(),
				roles.stream().map(this::toRoleSummary).toList(),
				user.getEmployee() != null ? user.getEmployee().getId() : null,
				user.getEmployee() != null ? user.getEmployee().getCode() : null,
				resolveEmployeeDisplayName(user),
				user.getCreatedAt(),
				user.getUpdatedAt()
		);
	}

	private UserRoleSummary toRoleSummary(Role role) {
		return new UserRoleSummary(role.getId(), role.getName(), role.isSystemRole());
	}

	private String resolveEmployeeDisplayName(User user) {
		if (user.getEmployee() == null) {
			return null;
		}
		String firstName = user.getEmployee().getFirstName();
		String lastName = user.getEmployee().getLastName();
		if (lastName == null || lastName.isBlank()) {
			return firstName;
		}
		return firstName + " " + lastName;
	}
}
