package pl.m2manager.security.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.m2manager.security.auth.dto.AuthContextResponse;
import pl.m2manager.security.auth.dto.AuthenticationResponse;
import pl.m2manager.security.auth.dto.ChangePasswordRequest;
import pl.m2manager.security.auth.dto.SwitchOrganizationRequest;
import pl.m2manager.security.jwt.JwtAuthenticatedPrincipal;
import pl.m2manager.security.jwt.JwtAuthenticationToken;

@RestController
@RequestMapping("/api/auth")
public class AuthContextController {

	private final AuthContextService authContextService;
	private final AuthSessionService authSessionService;
	private final AccountPasswordService accountPasswordService;

	public AuthContextController(
			AuthContextService authContextService,
			AuthSessionService authSessionService,
			AccountPasswordService accountPasswordService
	) {
		this.authContextService = authContextService;
		this.authSessionService = authSessionService;
		this.accountPasswordService = accountPasswordService;
	}

	@GetMapping("/context")
	@PreAuthorize("isAuthenticated()")
	public AuthContextResponse getContext() {
		return authContextService.getContext(requireAuthenticatedPrincipal());
	}

	@PostMapping("/context/organization")
	@PreAuthorize("isAuthenticated()")
	public AuthenticationResponse switchOrganization(
			@Valid @RequestBody SwitchOrganizationRequest request,
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse
	) {
		return authSessionService.switchOrganization(
				requireAuthenticatedPrincipal(),
				request.organizationId(),
				httpRequest,
				httpResponse
		);
	}

	@PostMapping("/change-password")
	@PreAuthorize("isAuthenticated()")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void changePassword(@Valid @RequestBody ChangePasswordRequest request) {
		accountPasswordService.changePassword(requireAuthenticatedPrincipal(), request);
	}

	private JwtAuthenticatedPrincipal requireAuthenticatedPrincipal() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
			return jwtAuthenticationToken.getPrincipal();
		}
		throw new IllegalStateException("Authenticated JWT principal required");
	}
}
