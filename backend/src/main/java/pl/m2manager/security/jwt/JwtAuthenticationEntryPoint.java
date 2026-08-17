package pl.m2manager.security.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
@Profile("!dev & !test")
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(
			HttpServletRequest request,
			HttpServletResponse response,
			AuthenticationException authException
	) throws IOException {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);

		String path = escapeJson(request.getRequestURI());
		String body = """
				{"timestamp":"%s","status":401,"error":"Unauthorized","message":"Unauthorized","path":"%s"}
				""".formatted(Instant.now(), path);
		response.getWriter().write(body);
	}

	private static String escapeJson(String value) {
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
