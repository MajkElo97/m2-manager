package pl.m2manager.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import pl.m2manager.common.entity.AuditableEntity;
import pl.m2manager.organization.entity.Organization;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "organization_id", nullable = false)
	private Organization organization;

	@NotBlank
	@Email
	@Size(max = 255)
	@Column(nullable = false, length = 255)
	private String email;

	@NotBlank
	@Column(name = "password_hash", nullable = false, length = 255)
	private String passwordHash;

	@Size(max = 255)
	@Column(name = "first_name", length = 255)
	private String firstName;

	@Size(max = 255)
	@Column(name = "last_name", length = 255)
	private String lastName;

	@Column(nullable = false)
	private boolean active = true;

	@Column(name = "last_login_at")
	private Instant lastLoginAt;

	public UUID getId() {
		return id;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
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

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Instant getLastLoginAt() {
		return lastLoginAt;
	}

	public void setLastLoginAt(Instant lastLoginAt) {
		this.lastLoginAt = lastLoginAt;
	}
}
