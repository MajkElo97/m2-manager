package pl.m2manager.security.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.m2manager.common.exception.GlobalExceptionHandler;
import pl.m2manager.security.auth.dto.AuthenticationResult;
import pl.m2manager.security.jwt.JwtService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthenticationController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthenticationControllerTest {

	private static final UUID USER_ID = UUID.fromString("b0000000-0000-4000-8000-000000000001");
	private static final UUID ORGANIZATION_ID = UUID.fromString("a0000000-0000-4000-8000-000000000002");

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AuthenticationService authenticationService;

	@MockitoBean
	private JwtService jwtService;

	@Test
	void login_validCredentials_returns200WithAccessToken() throws Exception {
		when(authenticationService.authenticate("org-a", "john@example.com", "passwordA"))
				.thenReturn(new AuthenticationResult(USER_ID, ORGANIZATION_ID, "john@example.com"));
		when(jwtService.generateAccessToken(any(AuthenticationResult.class))).thenReturn("jwt-access-token");
		when(jwtService.accessTokenExpirationSeconds()).thenReturn(900L);

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "organizationSlug": "org-a",
								  "email": "john@example.com",
								  "password": "passwordA"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").value("jwt-access-token"))
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.expiresIn").value(900))
				.andExpect(jsonPath("$.password").doesNotExist())
				.andExpect(jsonPath("$.passwordHash").doesNotExist());

		verify(authenticationService).authenticate("org-a", "john@example.com", "passwordA");
	}

	@Test
	void login_wrongPassword_returns401() throws Exception {
		when(authenticationService.authenticate(any(), any(), any()))
				.thenThrow(new BadCredentialsException(M2UserDetailsService.INVALID_CREDENTIALS_MESSAGE));

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "organizationSlug": "org-a",
								  "email": "john@example.com",
								  "password": "wrong-password"
								}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value(M2UserDetailsService.INVALID_CREDENTIALS_MESSAGE));
	}

	@Test
	void login_unknownOrganization_returns401() throws Exception {
		when(authenticationService.authenticate(any(), any(), any()))
				.thenThrow(new BadCredentialsException(M2UserDetailsService.INVALID_CREDENTIALS_MESSAGE));

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "organizationSlug": "unknown-slug",
								  "email": "john@example.com",
								  "password": "passwordA"
								}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value(M2UserDetailsService.INVALID_CREDENTIALS_MESSAGE));
	}

	@Test
	void login_unknownUser_returns401() throws Exception {
		when(authenticationService.authenticate(any(), any(), any()))
				.thenThrow(new BadCredentialsException(M2UserDetailsService.INVALID_CREDENTIALS_MESSAGE));

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "organizationSlug": "org-a",
								  "email": "missing@example.com",
								  "password": "passwordA"
								}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value(M2UserDetailsService.INVALID_CREDENTIALS_MESSAGE));
	}

	@Test
	void login_inactiveUser_returns401() throws Exception {
		when(authenticationService.authenticate(any(), any(), any()))
				.thenThrow(new BadCredentialsException(M2UserDetailsService.INVALID_CREDENTIALS_MESSAGE));

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "organizationSlug": "org-a",
								  "email": "inactive@example.com",
								  "password": "passwordA"
								}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value(M2UserDetailsService.INVALID_CREDENTIALS_MESSAGE));
	}
}
