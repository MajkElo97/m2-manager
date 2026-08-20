package pl.m2manager.activity.entity;

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
@Table(name = "activities")
public class Activity extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@NotBlank
	@Size(max = 100)
	@Column(nullable = false, length = 100)
	private String code;

	@Column(name = "organization_id")
	private UUID organizationId;

	@NotBlank
	@Size(max = 255)
	@Column(nullable = false, length = 255)
	private String name;

	@NotBlank
	@Size(max = 100)
	@Column(nullable = false, length = 100)
	private String category;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "planning_type", nullable = false, length = 20)
	private ActivityPlanningType planningType;

	@Size(max = 50)
	@Column(name = "default_period", length = 50)
	private String defaultPeriod;

	@Min(0)
	@Column(name = "duration_minutes")
	private Integer durationMinutes;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ActivityPriority priority;

	@Column(nullable = false)
	private boolean active = true;

	public UUID getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public ActivityPlanningType getPlanningType() {
		return planningType;
	}

	public void setPlanningType(ActivityPlanningType planningType) {
		this.planningType = planningType;
	}

	public String getDefaultPeriod() {
		return defaultPeriod;
	}

	public void setDefaultPeriod(String defaultPeriod) {
		this.defaultPeriod = defaultPeriod;
	}

	public Integer getDurationMinutes() {
		return durationMinutes;
	}

	public void setDurationMinutes(Integer durationMinutes) {
		this.durationMinutes = durationMinutes;
	}

	public ActivityPriority getPriority() {
		return priority;
	}

	public void setPriority(ActivityPriority priority) {
		this.priority = priority;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public UUID getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(UUID organizationId) {
		this.organizationId = organizationId;
	}

	public boolean isSystemActivity() {
		return organizationId == null;
	}
}
