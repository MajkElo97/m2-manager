package pl.m2manager.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtService jwtService;

	public JwtAuthenticationFilter(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

		if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
			String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
			if (!token.isEmpty()) {
				try {
					JwtAuthenticatedPrincipal principal = jwtService.parseAndValidate(token);
					SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(principal));
				} catch (InvalidJwtException ex) {
					SecurityContextHolder.clearContext();
				}
			}
		}

		filterChain.doFilter(request, response);
	}
}
