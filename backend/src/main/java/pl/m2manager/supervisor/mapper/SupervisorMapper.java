package pl.m2manager.supervisor.mapper;

import org.springframework.stereotype.Component;
import pl.m2manager.manager.entity.Manager;
import pl.m2manager.manager.repository.ManagerRepository;
import pl.m2manager.supervisor.dto.request.CreateSupervisorRequest;
import pl.m2manager.supervisor.dto.request.UpdateSupervisorRequest;
import pl.m2manager.supervisor.dto.response.SupervisorResponse;
import pl.m2manager.supervisor.entity.Supervisor;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class SupervisorMapper {

	private final ManagerRepository managerRepository;

	public SupervisorMapper(ManagerRepository managerRepository) {
		this.managerRepository = managerRepository;
	}

	public SupervisorResponse toResponse(Supervisor supervisor) {
		Manager manager = managerRepository.findById(supervisor.getManagerId()).orElse(null);
		return toResponse(supervisor, manager);
	}

	public List<SupervisorResponse> toResponseList(List<Supervisor> supervisors, UUID organizationId) {
		Map<UUID, Manager> managersById = managerRepository
				.findAllByIdInAndOrganizationId(
						supervisors.stream().map(Supervisor::getManagerId).collect(Collectors.toSet()),
						organizationId
				).stream()
				.collect(Collectors.toMap(Manager::getId, Function.identity()));

		return supervisors.stream()
				.map(supervisor -> toResponse(supervisor, managersById.get(supervisor.getManagerId())))
				.toList();
	}

	public void applyCreate(Supervisor supervisor, CreateSupervisorRequest request) {
		supervisor.setManagerId(request.managerId());
		supervisor.setCode(request.code());
		supervisor.setFirstName(request.firstName());
		supervisor.setLastName(request.lastName());
		supervisor.setPhone(request.phone());
		supervisor.setEmail(request.email());
		supervisor.setNotes(request.notes());
		supervisor.setActive(true);
	}

	public void applyUpdate(Supervisor supervisor, UpdateSupervisorRequest request) {
		supervisor.setManagerId(request.managerId());
		supervisor.setCode(request.code());
		supervisor.setFirstName(request.firstName());
		supervisor.setLastName(request.lastName());
		supervisor.setPhone(request.phone());
		supervisor.setEmail(request.email());
		supervisor.setNotes(request.notes());
		supervisor.setActive(request.active());
	}

	private SupervisorResponse toResponse(Supervisor supervisor, Manager manager) {
		return new SupervisorResponse(
				supervisor.getId(),
				supervisor.getManagerId(),
				manager != null ? manager.getCode() : null,
				manager != null ? manager.getName() : null,
				supervisor.getCode(),
				supervisor.getFirstName(),
				supervisor.getLastName(),
				supervisor.getPhone(),
				supervisor.getEmail(),
				supervisor.getNotes(),
				supervisor.isActive(),
				supervisor.getCreatedAt(),
				supervisor.getUpdatedAt()
		);
	}
}
