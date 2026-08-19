package pl.m2manager.inventory.entity;

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

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "chemicals")
public class Chemical extends AuditableEntity {

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

	@NotBlank
	@Size(max = 100)
	@Column(nullable = false, length = 100)
	private String category;

	@NotNull
	@Column(nullable = false, precision = 12, scale = 3)
	private BigDecimal quantity = BigDecimal.ZERO;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ChemicalUnit unit;

	@Column(name = "minimum_stock", precision = 12, scale = 3)
	private BigDecimal minimumStock;

	@Size(max = 255)
	@Column(length = 255)
	private String location;

	@Column(nullable = false)
	private boolean active = true;

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

	public BigDecimal getQuantity() {
		return quantity;
	}

	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}

	public ChemicalUnit getUnit() {
		return unit;
	}

	public void setUnit(ChemicalUnit unit) {
		this.unit = unit;
	}

	public BigDecimal getMinimumStock() {
		return minimumStock;
	}

	public void setMinimumStock(BigDecimal minimumStock) {
		this.minimumStock = minimumStock;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}
}
