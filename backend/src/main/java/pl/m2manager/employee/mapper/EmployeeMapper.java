package pl.m2manager.employee.mapper;

import org.springframework.stereotype.Component;
import pl.m2manager.employee.dto.request.CreateEmployeeRequest;
import pl.m2manager.employee.dto.request.UpdateEmployeeRequest;
import pl.m2manager.employee.dto.response.EmployeeResponse;
import pl.m2manager.employee.entity.Employee;

@Component
public class EmployeeMapper {

	public EmployeeResponse toResponse(Employee employee) {
		return new EmployeeResponse(
				employee.getId(),
				employee.getCode(),
				employee.getFirstName(),
				employee.getLastName(),
				employee.getPhone(),
				employee.getEmail(),
				employee.getGoogleEmail(),
				employee.getPosition(),
				employee.getRole(),
				employee.getEmploymentType(),
				employee.getEmploymentStartDate(),
				employee.getRemunerationAmount(),
				employee.getRemunerationUnit(),
				employee.getRemunerationNet(),
				employee.getCalendarColor(),
				employee.getNotes(),
				employee.isActive(),
				employee.getCreatedAt(),
				employee.getUpdatedAt()
		);
	}

	public void applyCreate(Employee employee, CreateEmployeeRequest request) {
		employee.setCode(request.code());
		employee.setFirstName(request.firstName());
		employee.setLastName(request.lastName());
		employee.setPhone(request.phone());
		employee.setEmail(request.email());
		employee.setGoogleEmail(request.googleEmail());
		employee.setPosition(request.position());
		employee.setRole(request.role());
		employee.setEmploymentType(request.employmentType());
		employee.setEmploymentStartDate(request.employmentStartDate());
		employee.setRemunerationAmount(request.remunerationAmount());
		employee.setRemunerationUnit(request.remunerationUnit());
		employee.setRemunerationNet(request.remunerationNet());
		employee.setCalendarColor(request.calendarColor());
		employee.setNotes(request.notes());
		employee.setActive(true);
	}

	public void applyUpdate(Employee employee, UpdateEmployeeRequest request) {
		employee.setCode(request.code());
		employee.setFirstName(request.firstName());
		employee.setLastName(request.lastName());
		employee.setPhone(request.phone());
		employee.setEmail(request.email());
		employee.setGoogleEmail(request.googleEmail());
		employee.setPosition(request.position());
		employee.setRole(request.role());
		employee.setEmploymentType(request.employmentType());
		employee.setEmploymentStartDate(request.employmentStartDate());
		employee.setRemunerationAmount(request.remunerationAmount());
		employee.setRemunerationUnit(request.remunerationUnit());
		employee.setRemunerationNet(request.remunerationNet());
		employee.setCalendarColor(request.calendarColor());
		employee.setNotes(request.notes());
		employee.setActive(request.active());
	}
}
