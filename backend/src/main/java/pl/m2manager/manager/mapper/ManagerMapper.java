package pl.m2manager.manager.mapper;

import org.springframework.stereotype.Component;
import pl.m2manager.manager.dto.request.CreateManagerRequest;
import pl.m2manager.manager.dto.request.UpdateManagerRequest;
import pl.m2manager.manager.dto.response.ManagerResponse;
import pl.m2manager.manager.entity.Manager;

@Component
public class ManagerMapper {

	public ManagerResponse toResponse(Manager manager, int supervisorCount) {
		return new ManagerResponse(
				manager.getId(),
				manager.getCode(),
				manager.getName(),
				manager.getPhone(),
				manager.getEmail(),
				manager.getAddress(),
				manager.getNotes(),
				manager.isActive(),
				supervisorCount,
				manager.getCreatedAt(),
				manager.getUpdatedAt()
		);
	}

	public void applyCreate(Manager manager, CreateManagerRequest request) {
		manager.setCode(request.code());
		manager.setName(request.name());
		manager.setPhone(request.phone());
		manager.setEmail(request.email());
		manager.setAddress(request.address());
		manager.setNotes(request.notes());
		manager.setActive(true);
	}

	public void applyUpdate(Manager manager, UpdateManagerRequest request) {
		manager.setCode(request.code());
		manager.setName(request.name());
		manager.setPhone(request.phone());
		manager.setEmail(request.email());
		manager.setAddress(request.address());
		manager.setNotes(request.notes());
	}
}
