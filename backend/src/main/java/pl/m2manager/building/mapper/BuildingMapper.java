package pl.m2manager.building.mapper;

import org.springframework.stereotype.Component;
import pl.m2manager.building.dto.request.CreateBuildingRequest;
import pl.m2manager.building.dto.request.UpdateBuildingRequest;
import pl.m2manager.building.dto.response.BuildingResponse;
import pl.m2manager.building.entity.Building;
import pl.m2manager.building.entity.BuildingStatus;

@Component
public class BuildingMapper {

	public BuildingResponse toResponse(Building building) {
		return new BuildingResponse(
				building.getId(),
				building.getCode(),
				building.getName(),
				building.getAddress(),
				building.getCity(),
				building.getNip(),
				building.getPhone(),
				building.getEmail(),
				resolveManagerCode(building),
				resolveSupervisorCode(building),
				resolveEmployeeCode(building),
				building.getContractSignedAt(),
				building.getServiceStartDate(),
				building.getNoticePeriodMonths(),
				building.getStatus(),
				building.getNotes(),
				building.getCreatedAt(),
				building.getUpdatedAt()
		);
	}

	private String resolveManagerCode(Building building) {
		if (building.getManager() != null) {
			return building.getManager().getCode();
		}
		return building.getManagerCode();
	}

	private String resolveSupervisorCode(Building building) {
		if (building.getSupervisor() != null) {
			return building.getSupervisor().getCode();
		}
		return building.getSupervisorCode();
	}

	private String resolveEmployeeCode(Building building) {
		if (building.getEmployee() != null) {
			return building.getEmployee().getCode();
		}
		return building.getEmployeeCode();
	}

	public void applyCreate(Building building, CreateBuildingRequest request) {
		building.setCode(request.code());
		building.setName(request.name());
		building.setAddress(request.address());
		building.setCity(request.city());
		building.setNip(request.nip());
		building.setPhone(request.phone());
		building.setEmail(request.email());
		building.setManagerCode(request.managerCode());
		building.setSupervisorCode(request.supervisorCode());
		building.setEmployeeCode(request.employeeCode());
		building.setContractSignedAt(request.contractSignedAt());
		building.setServiceStartDate(request.serviceStartDate());
		building.setNoticePeriodMonths(request.noticePeriodMonths());
		building.setStatus(BuildingStatus.ACTIVE);
		building.setNotes(request.notes());
	}

	public void applyUpdate(Building building, UpdateBuildingRequest request) {
		building.setCode(request.code());
		building.setName(request.name());
		building.setAddress(request.address());
		building.setCity(request.city());
		building.setNip(request.nip());
		building.setPhone(request.phone());
		building.setEmail(request.email());
		building.setManagerCode(request.managerCode());
		building.setSupervisorCode(request.supervisorCode());
		building.setEmployeeCode(request.employeeCode());
		building.setContractSignedAt(request.contractSignedAt());
		building.setServiceStartDate(request.serviceStartDate());
		building.setNoticePeriodMonths(request.noticePeriodMonths());
		building.setStatus(request.status());
		building.setNotes(request.notes());
	}
}
