package pl.m2manager.staircase.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pl.m2manager.common.entity.AuditableEntity;

import java.util.UUID;

@Entity
@Table(name = "staircases")
public class Staircase extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "organization_id", nullable = false)
	private UUID organizationId;

	@Column(name = "building_id", nullable = false)
	private UUID buildingId;

	@NotBlank
	@Size(max = 100)
	@Column(nullable = false, length = 100)
	private String code;

	@NotBlank
	@Size(max = 50)
	@Column(nullable = false, length = 50)
	private String designation;

	@Size(max = 255)
	@Column(name = "intercom_code", length = 255)
	private String intercomCode;

	@Column(name = "key_required", nullable = false)
	private boolean keyRequired = false;

	@Column(nullable = false)
	private boolean elevator = false;

	@NotNull
	@Min(0)
	@Max(200)
	@Column(nullable = false)
	private Integer floors;

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

	public UUID getBuildingId() {
		return buildingId;
	}

	public void setBuildingId(UUID buildingId) {
		this.buildingId = buildingId;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDesignation() {
		return designation;
	}

	public void setDesignation(String designation) {
		this.designation = designation;
	}

	public String getIntercomCode() {
		return intercomCode;
	}

	public void setIntercomCode(String intercomCode) {
		this.intercomCode = intercomCode;
	}

	public boolean isKeyRequired() {
		return keyRequired;
	}

	public void setKeyRequired(boolean keyRequired) {
		this.keyRequired = keyRequired;
	}

	public boolean isElevator() {
		return elevator;
	}

	public void setElevator(boolean elevator) {
		this.elevator = elevator;
	}

	public Integer getFloors() {
		return floors;
	}

	public void setFloors(Integer floors) {
		this.floors = floors;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}
}
