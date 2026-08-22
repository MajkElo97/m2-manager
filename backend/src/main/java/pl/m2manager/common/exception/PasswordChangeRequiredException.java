package pl.m2manager.common.exception;

public class PasswordChangeRequiredException extends RuntimeException {

	public PasswordChangeRequiredException() {
		super("Password change required");
	}
}
