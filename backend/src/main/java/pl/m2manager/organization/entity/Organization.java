package pl.m2manager.organization.entity;

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
@Table(name = "organizations")
public class Organization extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@NotBlank
	@Size(max = 255)
	@Column(nullable = false, length = 255)
	private String name;

	@NotBlank
	@Size(max = 100)
	@Column(nullable = false, length = 100, unique = true)
	private String slug;

	@Size(max = 20)
	@Column(length = 20)
	private String nip;

	@Email
	@Column(length = 255)
	private String email;

	@Size(max = 50)
	@Column(length = 50)
	private String phone;

	@Column(nullable = false)
	private boolean active = true;

	@Column(name = "system_organization", nullable = false)
	private boolean systemOrganization = false;

	@NotBlank
	@Size(max = 64)
	@Column(nullable = false, length = 64)
	private String timezone;

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public String getNip() {
		return nip;
	}

	public void setNip(String nip) {
		this.nip = nip;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isSystemOrganization() {
		return systemOrganization;
	}

	public void setSystemOrganization(boolean systemOrganization) {
		this.systemOrganization = systemOrganization;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}
}
