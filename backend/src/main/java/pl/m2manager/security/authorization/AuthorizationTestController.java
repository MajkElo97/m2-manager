package pl.m2manager.security.authorization;

import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Minimal endpoint for verifying {@code @PreAuthorize} integration in jwt-it tests only.
 * Not exposed in dev/test profiles.
 */
@RestController
@Profile("jwt-it")
@RequestMapping("/api/test/authorization")
public class AuthorizationTestController {

	@GetMapping("/buildings-view")
	@PreAuthorize("@authorizationService.hasPermission('BUILDINGS_VIEW')")
	public Map<String, String> buildingsView() {
		return Map.of("status", "ok");
	}
}
