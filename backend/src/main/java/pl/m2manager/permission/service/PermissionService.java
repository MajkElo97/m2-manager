package pl.m2manager.permission.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.m2manager.common.exception.ResourceNotFoundException;
import pl.m2manager.permission.dto.PermissionResponse;
import pl.m2manager.permission.entity.Permission;
import pl.m2manager.permission.mapper.PermissionMapper;
import pl.m2manager.permission.repository.PermissionRepository;

import java.util.Collection;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PermissionService {

	private final PermissionRepository permissionRepository;
	private final PermissionMapper permissionMapper;

	public PermissionService(PermissionRepository permissionRepository, PermissionMapper permissionMapper) {
		this.permissionRepository = permissionRepository;
		this.permissionMapper = permissionMapper;
	}

	public PermissionResponse findByCode(String code) {
		return permissionMapper.toResponse(findPermissionEntityByCode(code));
	}

	public Permission findPermissionEntityByCode(String code) {
		return permissionRepository.findByCode(code)
				.orElseThrow(() -> new ResourceNotFoundException("Permission", code));
	}

	public List<PermissionResponse> findByModule(String module) {
		return permissionRepository.findAllByModule(module).stream()
				.map(permissionMapper::toResponse)
				.toList();
	}

	public List<PermissionResponse> findByCodes(Collection<String> codes) {
		return permissionRepository.findAllByCodeIn(codes).stream()
				.map(permissionMapper::toResponse)
				.toList();
	}

	public List<PermissionResponse> listAll() {
		return permissionRepository.findAll().stream()
				.map(permissionMapper::toResponse)
				.toList();
	}
}
