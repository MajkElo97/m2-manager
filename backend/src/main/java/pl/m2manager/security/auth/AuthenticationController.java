package pl.m2manager.security.auth;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.m2manager.security.auth.dto.AuthenticationRequest;
import pl.m2manager.security.auth.dto.AuthenticationResponse;
import pl.m2manager.security.auth.dto.AuthenticationResult;
import pl.m2manager.security.jwt.JwtService;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

	private final AuthenticationService authenticationService;
	private final JwtService jwtService;

	public AuthenticationController(AuthenticationService authenticationService, JwtService jwtService) {
		this.authenticationService = authenticationService;
		this.jwtService = jwtService;
	}

	@PostMapping("/login")
	public AuthenticationResponse login(@Valid @RequestBody AuthenticationRequest request) {
		AuthenticationResult result = authenticationService.authenticate(
				request.organizationSlug(),
				request.email(),
				request.password()
		);

		return new AuthenticationResponse(
				jwtService.generateAccessToken(result),
				"Bearer",
				jwtService.accessTokenExpirationSeconds()
		);
	}
}
