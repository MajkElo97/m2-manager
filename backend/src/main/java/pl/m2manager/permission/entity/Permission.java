package pl.m2manager.permission.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Entity
@Table(name = "permissions")
public class Permission {

	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@NotBlank
	@Size(max = 100)
	@Column(nullable = false, length = 100, unique = true)
	private String code;

	@NotBlank
	@Size(max = 50)
	@Column(nullable = false, length = 50)
	private String module;

	@NotBlank
	@Size(max = 20)
	@Column(nullable = false, length = 20)
	private String action;

	@Size(max = 255)
	@Column(length = 255)
	private String description;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
