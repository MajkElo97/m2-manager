package pl.m2manager.security.jwt;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

	private final JwtAuthenticatedPrincipal principal;

	public JwtAuthenticationToken(JwtAuthenticatedPrincipal principal) {
		super(AuthorityUtils.NO_AUTHORITIES);
		this.principal = principal;
		setAuthenticated(true);
	}

	@Override
	public Object getCredentials() {
		return null;
	}

	@Override
	public JwtAuthenticatedPrincipal getPrincipal() {
		return principal;
	}
}
