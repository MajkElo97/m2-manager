package pl.m2manager.employee.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import pl.m2manager.common.entity.AuditableEntity;
import pl.m2manager.organization.entity.Organization;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "employees")
public class Employee extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "organization_id", nullable = false)
	private Organization organization;

	@NotBlank
	@Size(max = 50)
	@Column(nullable = false, length = 50)
	private String code;

	@NotBlank
	@Size(max = 100)
	@Column(name = "first_name", nullable = false, length = 100)
	private String firstName;

	@Size(max = 100)
	@Column(name = "last_name", length = 100)
	private String lastName;

	@Size(max = 50)
	@Column(length = 50)
	private String phone;

	@Email
	@Size(max = 255)
	@Column(length = 255)
	private String email;

	@Email
	@Size(max = 255)
	@Column(name = "google_email", length = 255)
	private String googleEmail;

	@Size(max = 100)
	@Column(length = 100)
	private String position;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private EmployeeRole role;

	@Enumerated(EnumType.STRING)
	@Column(name = "employment_type", length = 20)
	private EmploymentType employmentType;

	@Column(name = "employment_start_date")
	private LocalDate employmentStartDate;

	@Column(name = "remuneration_amount", precision = 10, scale = 2)
	private BigDecimal remunerationAmount;

	@Enumerated(EnumType.STRING)
	@Column(name = "remuneration_unit", length = 20)
	private RemunerationUnit remunerationUnit;

	@Column(name = "remuneration_net")
	private Boolean remunerationNet;

	@Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
	@Size(max = 7)
	@Column(name = "calendar_color", length = 7)
	private String calendarColor;

	@Column(columnDefinition = "TEXT")
	private String notes;

	@Column(nullable = false)
	private boolean active = true;

	public UUID getId() {
		return id;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
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

	public String getGoogleEmail() {
		return googleEmail;
	}

	public void setGoogleEmail(String googleEmail) {
		this.googleEmail = googleEmail;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public EmployeeRole getRole() {
		return role;
	}

	public void setRole(EmployeeRole role) {
		this.role = role;
	}

	public EmploymentType getEmploymentType() {
		return employmentType;
	}

	public void setEmploymentType(EmploymentType employmentType) {
		this.employmentType = employmentType;
	}

	public LocalDate getEmploymentStartDate() {
		return employmentStartDate;
	}

	public void setEmploymentStartDate(LocalDate employmentStartDate) {
		this.employmentStartDate = employmentStartDate;
	}

	public BigDecimal getRemunerationAmount() {
		return remunerationAmount;
	}

	public void setRemunerationAmount(BigDecimal remunerationAmount) {
		this.remunerationAmount = remunerationAmount;
	}

	public RemunerationUnit getRemunerationUnit() {
		return remunerationUnit;
	}

	public void setRemunerationUnit(RemunerationUnit remunerationUnit) {
		this.remunerationUnit = remunerationUnit;
	}

	public Boolean getRemunerationNet() {
		return remunerationNet;
	}

	public void setRemunerationNet(Boolean remunerationNet) {
		this.remunerationNet = remunerationNet;
	}

	public String getCalendarColor() {
		return calendarColor;
	}

	public void setCalendarColor(String calendarColor) {
		this.calendarColor = calendarColor;
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
