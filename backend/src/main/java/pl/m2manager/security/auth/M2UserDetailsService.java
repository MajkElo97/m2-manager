package pl.m2manager.security.auth;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.m2manager.organization.repository.OrganizationRepository;
import pl.m2manager.user.repository.UserRepository;

@Service
public class M2UserDetailsService {

	static final String INVALID_CREDENTIALS_MESSAGE = "Invalid credentials";

	private final OrganizationRepository organizationRepository;
	private final UserRepository userRepository;

	public M2UserDetailsService(OrganizationRepository organizationRepository, UserRepository userRepository) {
		this.organizationRepository = organizationRepository;
		this.userRepository = userRepository;
	}

	@Transactional(readOnly = true)
	public AuthenticatedUser loadByOrganizationSlugAndEmail(String organizationSlug, String email) {
		var organization = organizationRepository.findBySlug(organizationSlug)
				.orElseThrow(() -> new UsernameNotFoundException(INVALID_CREDENTIALS_MESSAGE));

		var user = userRepository.findByOrganizationIdAndEmail(organization.getId(), email)
				.orElseThrow(() -> new UsernameNotFoundException(INVALID_CREDENTIALS_MESSAGE));

		return AuthenticatedUser.from(user);
	}
}
