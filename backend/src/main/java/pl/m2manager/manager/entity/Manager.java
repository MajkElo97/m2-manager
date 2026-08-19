package pl.m2manager.manager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import pl.m2manager.common.entity.AuditableEntity;

import java.util.UUID;

@Entity
@Table(name = "managers")
public class Manager extends AuditableEntity {

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
	@Size(max = 255)
	@Column(nullable = false, length = 255)
	private String name;

	@Size(max = 50)
	@Column(length = 50)
	private String phone;

	@Size(max = 255)
	@Column(length = 255)
	private String email;

	@Size(max = 500)
	@Column(length = 500)
	private String address;

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

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
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
