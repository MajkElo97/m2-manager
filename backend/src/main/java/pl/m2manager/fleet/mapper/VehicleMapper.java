package pl.m2manager.fleet.mapper;

import org.springframework.stereotype.Component;
import pl.m2manager.employee.entity.Employee;
import pl.m2manager.fleet.dto.request.CreateVehicleRequest;
import pl.m2manager.fleet.dto.request.UpdateVehicleRequest;
import pl.m2manager.fleet.dto.response.VehicleResponse;
import pl.m2manager.fleet.entity.Vehicle;
import pl.m2manager.fleet.entity.VehicleStatus;

import java.util.UUID;

@Component
public class VehicleMapper {

	public VehicleResponse toResponse(Vehicle vehicle, Employee employee) {
		return new VehicleResponse(
				vehicle.getId(),
				vehicle.getCode(),
				vehicle.getRegistrationNumber(),
				vehicle.getMake(),
				vehicle.getModel(),
				vehicle.getProductionYear(),
				vehicle.getVin(),
				vehicle.getVehicleType(),
				vehicle.getEmployeeId(),
				employee != null ? employee.getCode() : null,
				employee != null ? formatEmployeeName(employee) : null,
				vehicle.getStatus(),
				vehicle.getInsuranceStartDate(),
				vehicle.getInsuranceEndDate(),
				vehicle.getInsurer(),
				vehicle.getInsurancePolicyNumber(),
				vehicle.getLastInspectionDate(),
				vehicle.getNextInspectionDate(),
				vehicle.getLastInspectionMileage(),
				vehicle.getPurchaseDate(),
				vehicle.getCurrentMileage(),
				vehicle.getNotes(),
				vehicle.getCreatedAt(),
				vehicle.getUpdatedAt()
		);
	}

	public void applyCreate(Vehicle vehicle, CreateVehicleRequest request, UUID employeeId) {
		vehicle.setCode(request.code());
		vehicle.setRegistrationNumber(request.registrationNumber());
		vehicle.setMake(request.make());
		vehicle.setModel(request.model());
		vehicle.setProductionYear(request.productionYear());
		vehicle.setVin(request.vin());
		vehicle.setVehicleType(request.vehicleType());
		vehicle.setEmployeeId(employeeId);
		vehicle.setStatus(request.status());
		vehicle.setInsuranceStartDate(request.insuranceStartDate());
		vehicle.setInsuranceEndDate(request.insuranceEndDate());
		vehicle.setInsurer(request.insurer());
		vehicle.setInsurancePolicyNumber(request.insurancePolicyNumber());
		vehicle.setLastInspectionDate(request.lastInspectionDate());
		vehicle.setNextInspectionDate(request.nextInspectionDate());
		vehicle.setLastInspectionMileage(request.lastInspectionMileage());
		vehicle.setPurchaseDate(request.purchaseDate());
		vehicle.setCurrentMileage(request.currentMileage());
		vehicle.setNotes(request.notes());
	}

	public void applyUpdate(Vehicle vehicle, UpdateVehicleRequest request, UUID employeeId) {
		vehicle.setCode(request.code());
		vehicle.setRegistrationNumber(request.registrationNumber());
		vehicle.setMake(request.make());
		vehicle.setModel(request.model());
		vehicle.setProductionYear(request.productionYear());
		vehicle.setVin(request.vin());
		vehicle.setVehicleType(request.vehicleType());
		vehicle.setEmployeeId(employeeId);
		vehicle.setStatus(request.status());
		vehicle.setInsuranceStartDate(request.insuranceStartDate());
		vehicle.setInsuranceEndDate(request.insuranceEndDate());
		vehicle.setInsurer(request.insurer());
		vehicle.setInsurancePolicyNumber(request.insurancePolicyNumber());
		vehicle.setLastInspectionDate(request.lastInspectionDate());
		vehicle.setNextInspectionDate(request.nextInspectionDate());
		vehicle.setLastInspectionMileage(request.lastInspectionMileage());
		vehicle.setPurchaseDate(request.purchaseDate());
		vehicle.setCurrentMileage(request.currentMileage());
		vehicle.setNotes(request.notes());
	}

	private String formatEmployeeName(Employee employee) {
		String lastName = employee.getLastName();
		if (lastName == null || lastName.isBlank()) {
			return employee.getFirstName();
		}
		return employee.getFirstName() + " " + lastName;
	}
}
