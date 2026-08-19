package pl.m2manager.fleet.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pl.m2manager.common.entity.AuditableEntity;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "vehicles")
public class Vehicle extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "organization_id", nullable = false)
	private UUID organizationId;

	@NotBlank
	@Size(max = 50)
	@Column(nullable = false, length = 50)
	private String code;

	@NotBlank
	@Size(max = 20)
	@Column(name = "registration_number", nullable = false, length = 20)
	private String registrationNumber;

	@NotBlank
	@Size(max = 100)
	@Column(nullable = false, length = 100)
	private String make;

	@NotBlank
	@Size(max = 100)
	@Column(nullable = false, length = 100)
	private String model;

	@Column(name = "production_year")
	private Integer productionYear;

	@Size(max = 50)
	@Column(length = 50)
	private String vin;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "vehicle_type", nullable = false, length = 20)
	private VehicleType vehicleType;

	@Column(name = "employee_id")
	private UUID employeeId;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private VehicleStatus status = VehicleStatus.ACTIVE;

	@Column(name = "insurance_start_date")
	private LocalDate insuranceStartDate;

	@Column(name = "insurance_end_date")
	private LocalDate insuranceEndDate;

	@Size(max = 255)
	@Column(length = 255)
	private String insurer;

	@Size(max = 100)
	@Column(name = "insurance_policy_number", length = 100)
	private String insurancePolicyNumber;

	@Column(name = "last_inspection_date")
	private LocalDate lastInspectionDate;

	@Column(name = "next_inspection_date")
	private LocalDate nextInspectionDate;

	@Column(name = "last_inspection_mileage")
	private Integer lastInspectionMileage;

	@Column(name = "purchase_date")
	private LocalDate purchaseDate;

	@Column(name = "current_mileage")
	private Integer currentMileage;

	@Column(columnDefinition = "TEXT")
	private String notes;

	public UUID getId() {
		return id;
	}

	public UUID getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(UUID organizationId) {
		this.organizationId = organizationId;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getRegistrationNumber() {
		return registrationNumber;
	}

	public void setRegistrationNumber(String registrationNumber) {
		this.registrationNumber = registrationNumber;
	}

	public String getMake() {
		return make;
	}

	public void setMake(String make) {
		this.make = make;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public Integer getProductionYear() {
		return productionYear;
	}

	public void setProductionYear(Integer productionYear) {
		this.productionYear = productionYear;
	}

	public String getVin() {
		return vin;
	}

	public void setVin(String vin) {
		this.vin = vin;
	}

	public VehicleType getVehicleType() {
		return vehicleType;
	}

	public void setVehicleType(VehicleType vehicleType) {
		this.vehicleType = vehicleType;
	}

	public UUID getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(UUID employeeId) {
		this.employeeId = employeeId;
	}

	public VehicleStatus getStatus() {
		return status;
	}

	public void setStatus(VehicleStatus status) {
		this.status = status;
	}

	public LocalDate getInsuranceStartDate() {
		return insuranceStartDate;
	}

	public void setInsuranceStartDate(LocalDate insuranceStartDate) {
		this.insuranceStartDate = insuranceStartDate;
	}

	public LocalDate getInsuranceEndDate() {
		return insuranceEndDate;
	}

	public void setInsuranceEndDate(LocalDate insuranceEndDate) {
		this.insuranceEndDate = insuranceEndDate;
	}

	public String getInsurer() {
		return insurer;
	}

	public void setInsurer(String insurer) {
		this.insurer = insurer;
	}

	public String getInsurancePolicyNumber() {
		return insurancePolicyNumber;
	}

	public void setInsurancePolicyNumber(String insurancePolicyNumber) {
		this.insurancePolicyNumber = insurancePolicyNumber;
	}

	public LocalDate getLastInspectionDate() {
		return lastInspectionDate;
	}

	public void setLastInspectionDate(LocalDate lastInspectionDate) {
		this.lastInspectionDate = lastInspectionDate;
	}

	public LocalDate getNextInspectionDate() {
		return nextInspectionDate;
	}

	public void setNextInspectionDate(LocalDate nextInspectionDate) {
		this.nextInspectionDate = nextInspectionDate;
	}

	public Integer getLastInspectionMileage() {
		return lastInspectionMileage;
	}

	public void setLastInspectionMileage(Integer lastInspectionMileage) {
		this.lastInspectionMileage = lastInspectionMileage;
	}

	public LocalDate getPurchaseDate() {
		return purchaseDate;
	}

	public void setPurchaseDate(LocalDate purchaseDate) {
		this.purchaseDate = purchaseDate;
	}

	public Integer getCurrentMileage() {
		return currentMileage;
	}

	public void setCurrentMileage(Integer currentMileage) {
		this.currentMileage = currentMileage;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}
}
