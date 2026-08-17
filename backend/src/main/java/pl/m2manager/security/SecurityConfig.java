package pl.m2manager.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import pl.m2manager.security.jwt.JwtAccessDeniedHandler;
import pl.m2manager.security.jwt.JwtAuthenticationEntryPoint;
import pl.m2manager.security.jwt.JwtAuthenticationFilter;
import pl.m2manager.security.jwt.JwtService;

/**
 * Production security configuration with JWT bearer authentication.
 *
 * CSRF: Bearer-authenticated API requests do not rely on cookies and are exempt from CSRF checks.
 * Refresh and logout use the HttpOnly refresh-token cookie and therefore require a valid CSRF token.
 * Login is exempt because no refresh cookie exists yet.
 */
@Configuration
@Profile("!dev & !test")
public class SecurityConfig {

	@Bean
	JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService) {
		return new JwtAuthenticationFilter(jwtService);
	}

	@Bean
	SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			JwtAuthenticationFilter jwtAuthenticationFilter,
			JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
			JwtAccessDeniedHandler jwtAccessDeniedHandler
	) throws Exception {
		CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
		csrfTokenRepository.setCookiePath("/api");

		CsrfTokenRequestAttributeHandler csrfRequestHandler = new CsrfTokenRequestAttributeHandler();
		csrfRequestHandler.setCsrfRequestAttributeName(null);

		http
				.csrf(csrf -> csrf
						.csrfTokenRepository(csrfTokenRepository)
						.csrfTokenRequestHandler(csrfRequestHandler)
						.ignoringRequestMatchers(
								"/actuator/health",
								"/actuator/health/**"
						)
						.ignoringRequestMatchers(request -> {
							String authorization = request.getHeader("Authorization");
							return authorization != null && authorization.startsWith("Bearer ");
						})
						.ignoringRequestMatchers(request ->
								HttpMethod.POST.matches(request.getMethod())
										&& "/api/auth/login".equals(request.getRequestURI())
						)
				)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/refresh", "/api/auth/logout")
						.permitAll()
						.requestMatchers("/api/**").authenticated()
						.anyRequest().denyAll()
				)
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint(jwtAuthenticationEntryPoint)
						.accessDeniedHandler(jwtAccessDeniedHandler))
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.httpBasic(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable);

		return http.build();
	}
}
