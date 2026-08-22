package pl.m2manager.security.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.m2manager.security.jwt.JwtAuthenticatedPrincipal;
import pl.m2manager.security.jwt.JwtAuthenticationToken;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MustChangePasswordFilter extends OncePerRequestFilter {

	private static final String PASSWORD_CHANGE_REQUIRED_JSON = """
			{"timestamp":null,"status":403,"error":"Forbidden","message":"Password change required","path":null,"fieldErrors":null}
			""";

	private final AccountPasswordService accountPasswordService;

	public MustChangePasswordFilter(AccountPasswordService accountPasswordService) {
		this.accountPasswordService = accountPasswordService;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		if (isAllowedDuringPasswordChange(request)) {
			filterChain.doFilter(request, response);
			return;
		}

		var authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
			JwtAuthenticatedPrincipal principal = jwtAuthenticationToken.getPrincipal();
			if (accountPasswordService.mustChangePassword(principal.userId())) {
				response.setStatus(HttpStatus.FORBIDDEN.value());
				response.setContentType(MediaType.APPLICATION_JSON_VALUE);
				response.setCharacterEncoding(StandardCharsets.UTF_8.name());
				response.getWriter().write(PASSWORD_CHANGE_REQUIRED_JSON.replace(
						"\"path\":null",
						"\"path\":\"" + request.getRequestURI() + "\""
				));
				return;
			}
		}

		filterChain.doFilter(request, response);
	}

	private boolean isAllowedDuringPasswordChange(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String method = request.getMethod();

		if ("POST".equals(method) && "/api/auth/change-password".equals(uri)) {
			return true;
		}
		if ("POST".equals(method) && "/api/auth/logout".equals(uri)) {
			return true;
		}
		if ("POST".equals(method) && "/api/auth/refresh".equals(uri)) {
			return true;
		}
		if ("GET".equals(method) && "/api/auth/context".equals(uri)) {
			return true;
		}
		return false;
	}
}
