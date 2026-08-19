package pl.m2manager.building.entity;

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
import pl.m2manager.employee.entity.Employee;
import pl.m2manager.manager.entity.Manager;
import pl.m2manager.supervisor.entity.Supervisor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pl.m2manager.common.entity.AuditableEntity;
import pl.m2manager.employee.entity.Employee;
import pl.m2manager.manager.entity.Manager;
import pl.m2manager.organization.entity.Organization;
import pl.m2manager.supervisor.entity.Supervisor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "buildings")
public class Building extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "organization_id", nullable = false)
	private Organization organization;

	@NotBlank
	@Size(max = 100)
	@Column(nullable = false, length = 100)
	private String code;

	@NotBlank
	@Size(max = 255)
	@Column(nullable = false, length = 255)
	private String name;

	@NotBlank
	@Size(max = 255)
	@Column(nullable = false, length = 255)
	private String address;

	@NotBlank
	@Size(max = 100)
	@Column(nullable = false, length = 100)
	private String city;

	@Size(max = 20)
	@Column(length = 20)
	private String nip;

	@Size(max = 50)
	@Column(length = 50)
	private String phone;

	@Email
	@Size(max = 255)
	@Column(length = 255)
	private String email;

	@Size(max = 50)
	@Column(name = "manager_code", length = 50)
	private String managerCode;

	@Size(max = 50)
	@Column(name = "supervisor_code", length = 50)
	private String supervisorCode;

	@Size(max = 50)
	@Column(name = "employee_code", length = 50)
	private String employeeCode;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "manager_id")
	private Manager manager;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "supervisor_id")
	private Supervisor supervisor;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "employee_id")
	private Employee employee;

	@Column(name = "contract_signed_at")
	private LocalDate contractSignedAt;

	@Column(name = "service_start_date")
	private LocalDate serviceStartDate;

	@NotNull
	@Min(0)
	@Max(120)
	@Column(name = "notice_period_months", nullable = false)
	private Integer noticePeriodMonths;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private BuildingStatus status = BuildingStatus.ACTIVE;

	@Column(columnDefinition = "TEXT")
	private String notes;

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getNip() {
		return nip;
	}

	public void setNip(String nip) {
		this.nip = nip;
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

	public String getManagerCode() {
		return managerCode;
	}

	public void setManagerCode(String managerCode) {
		this.managerCode = managerCode;
	}

	public String getSupervisorCode() {
		return supervisorCode;
	}

	public void setSupervisorCode(String supervisorCode) {
		this.supervisorCode = supervisorCode;
	}

	public String getEmployeeCode() {
		return employeeCode;
	}

	public void setEmployeeCode(String employeeCode) {
		this.employeeCode = employeeCode;
	}

	public Manager getManager() {
		return manager;
	}

	public void setManager(Manager manager) {
		this.manager = manager;
	}

	public Supervisor getSupervisor() {
		return supervisor;
	}

	public void setSupervisor(Supervisor supervisor) {
		this.supervisor = supervisor;
	}

	public Employee getEmployee() {
		return employee;
	}

	public void setEmployee(Employee employee) {
		this.employee = employee;
	}

	public LocalDate getContractSignedAt() {
		return contractSignedAt;
	}

	public void setContractSignedAt(LocalDate contractSignedAt) {
		this.contractSignedAt = contractSignedAt;
	}

	public LocalDate getServiceStartDate() {
		return serviceStartDate;
	}

	public void setServiceStartDate(LocalDate serviceStartDate) {
		this.serviceStartDate = serviceStartDate;
	}

	public Integer getNoticePeriodMonths() {
		return noticePeriodMonths;
	}

	public void setNoticePeriodMonths(Integer noticePeriodMonths) {
		this.noticePeriodMonths = noticePeriodMonths;
	}

	public BuildingStatus getStatus() {
		return status;
	}

	public void setStatus(BuildingStatus status) {
		this.status = status;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}
}
