package pl.m2manager.supervisor.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import pl.m2manager.common.entity.AuditableEntity;

import java.util.UUID;

@Entity
@Table(name = "supervisors")
public class Supervisor extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "organization_id", nullable = false)
	private UUID organizationId;

	@Column(name = "manager_id", nullable = false)
	private UUID managerId;

	@NotBlank
	@Size(max = 50)
	@Column(nullable = false, length = 50)
	private String code;

	@NotBlank
	@Size(max = 100)
	@Column(name = "first_name", nullable = false, length = 100)
	private String firstName;

	@NotBlank
	@Size(max = 100)
	@Column(name = "last_name", nullable = false, length = 100)
	private String lastName;

	@Size(max = 50)
	@Column(length = 50)
	private String phone;

	@Email
	@Size(max = 255)
	@Column(length = 255)
	private String email;

	@Column(columnDefinition = "TEXT")
	private String notes;

	@Column(nullable = false)
	private boolean active = true;

	public UUID getId() {
		return id;
	}

	public UUID getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(UUID organizationId) {
		this.organizationId = organizationId;
	}

	public UUID getManagerId() {
		return managerId;
	}

	public void setManagerId(UUID managerId) {
		this.managerId = managerId;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}
