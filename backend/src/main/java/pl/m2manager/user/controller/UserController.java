package pl.m2manager.user.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.m2manager.user.dto.CreateUserRequest;
import pl.m2manager.user.dto.UpdateUserRequest;
import pl.m2manager.user.dto.UserResponse;
import pl.m2manager.user.service.UserService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping
	@PreAuthorize("@authorizationService.hasPermission('USERS_VIEW')")
	public List<UserResponse> list(
			@RequestParam(required = false) String search,
			@RequestParam(required = false) Boolean active,
			@RequestParam(required = false) UUID roleId
	) {
		return userService.getAll(search, active, roleId);
	}

	@GetMapping("/{id}")
	@PreAuthorize("@authorizationService.hasPermission('USERS_VIEW')")
	public UserResponse getById(@PathVariable UUID id) {
		return userService.getById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("@authorizationService.hasPermission('USERS_CREATE')")
	public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
		return userService.create(request);
	}

	@PutMapping("/{id}")
	@PreAuthorize("@authorizationService.hasPermission('USERS_EDIT')")
	public UserResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
		return userService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("@authorizationService.hasPermission('USERS_DELETE')")
	public void deactivate(@PathVariable UUID id) {
		userService.deactivate(id);
	}
}
