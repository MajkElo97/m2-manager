package pl.m2manager.security.jwt;

public class InvalidRefreshTokenException extends RuntimeException {

	public static final String MESSAGE = "Invalid refresh token";

	public InvalidRefreshTokenException() {
		super(MESSAGE);
	}
}
