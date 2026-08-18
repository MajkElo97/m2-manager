package pl.m2manager.staircase.mapper;

import org.springframework.stereotype.Component;
import pl.m2manager.staircase.dto.request.CreateStaircaseRequest;
import pl.m2manager.staircase.dto.request.UpdateStaircaseRequest;
import pl.m2manager.staircase.dto.response.StaircaseResponse;
import pl.m2manager.staircase.entity.Staircase;

@Component
public class StaircaseMapper {

	public StaircaseResponse toResponse(Staircase staircase) {
		return new StaircaseResponse(
				staircase.getId(),
				staircase.getBuildingId(),
				staircase.getCode(),
				staircase.getDesignation(),
				staircase.getIntercomCode(),
				staircase.isKeyRequired(),
				staircase.isElevator(),
				staircase.getFloors(),
				staircase.getNotes(),
				staircase.getCreatedAt(),
				staircase.getUpdatedAt()
		);
	}

	public void applyCreate(Staircase staircase, CreateStaircaseRequest request) {
		staircase.setCode(request.code());
		staircase.setDesignation(request.designation());
		staircase.setIntercomCode(request.intercomCode());
		staircase.setKeyRequired(request.keyRequired());
		staircase.setElevator(request.elevator());
		staircase.setFloors(request.floors());
		staircase.setNotes(request.notes());
	}

	public void applyUpdate(Staircase staircase, UpdateStaircaseRequest request) {
		staircase.setCode(request.code());
		staircase.setDesignation(request.designation());
		staircase.setIntercomCode(request.intercomCode());
		staircase.setKeyRequired(request.keyRequired());
		staircase.setElevator(request.elevator());
		staircase.setFloors(request.floors());
		staircase.setNotes(request.notes());
	}
}
