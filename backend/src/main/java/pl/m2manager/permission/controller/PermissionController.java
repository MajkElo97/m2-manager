package pl.m2manager.permission.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.m2manager.permission.dto.PermissionResponse;
import pl.m2manager.permission.service.PermissionService;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

	private final PermissionService permissionService;

	public PermissionController(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	@GetMapping
	@PreAuthorize("@authorizationService.hasPermission('ROLES_VIEW')")
	public List<PermissionResponse> listAll() {
		return permissionService.listAll();
	}
}
