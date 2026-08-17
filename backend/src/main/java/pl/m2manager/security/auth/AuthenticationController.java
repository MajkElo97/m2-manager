package pl.m2manager.security.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.m2manager.security.auth.dto.AuthenticationRequest;
import pl.m2manager.security.auth.dto.AuthenticationResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

	private final AuthSessionService authSessionService;

	public AuthenticationController(AuthSessionService authSessionService) {
		this.authSessionService = authSessionService;
	}

	@PostMapping("/login")
	public AuthenticationResponse login(
			@Valid @RequestBody AuthenticationRequest request,
			HttpServletResponse response
	) {
		return authSessionService.login(
				request.organizationSlug(),
				request.email(),
				request.password(),
				response
		);
	}

	@PostMapping("/refresh")
	public AuthenticationResponse refresh(HttpServletRequest request, HttpServletResponse response) {
		return authSessionService.refresh(request, response);
	}

	@PostMapping("/logout")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void logout(HttpServletRequest request, HttpServletResponse response) {
		authSessionService.logout(request, response);
	}
}
