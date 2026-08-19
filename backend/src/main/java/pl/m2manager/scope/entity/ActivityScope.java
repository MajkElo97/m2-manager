package pl.m2manager.scope.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pl.m2manager.common.entity.AuditableEntity;

import java.util.UUID;

@Entity
@Table(name = "activity_scopes")
public class ActivityScope extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "organization_id", nullable = false)
	private UUID organizationId;

	@NotBlank
	@Size(max = 100)
	@Column(nullable = false, length = 100)
	private String code;

	@Column(name = "building_id", nullable = false)
	private UUID buildingId;

	@Column(name = "activity_id", nullable = false)
	private UUID activityId;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "planning_type", nullable = false, length = 20)
	private ScopePlanningType planningType;

	@Min(0)
	private Integer frequency;

	@Size(max = 255)
	private String weekdays;

	@Column(columnDefinition = "TEXT")
	private String notes;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ScopeStatus status = ScopeStatus.ACTIVE;

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

	public UUID getBuildingId() {
		return buildingId;
	}

	public void setBuildingId(UUID buildingId) {
		this.buildingId = buildingId;
	}

	public UUID getActivityId() {
		return activityId;
	}

	public void setActivityId(UUID activityId) {
		this.activityId = activityId;
	}

	public ScopePlanningType getPlanningType() {
		return planningType;
	}

	public void setPlanningType(ScopePlanningType planningType) {
		this.planningType = planningType;
	}

	public Integer getFrequency() {
		return frequency;
	}

	public void setFrequency(Integer frequency) {
		this.frequency = frequency;
	}

	public String getWeekdays() {
		return weekdays;
	}

	public void setWeekdays(String weekdays) {
		this.weekdays = weekdays;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public ScopeStatus getStatus() {
		return status;
	}

	public void setStatus(ScopeStatus status) {
		this.status = status;
	}
}
