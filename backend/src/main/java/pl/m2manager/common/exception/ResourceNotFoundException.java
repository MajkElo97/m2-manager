package pl.m2manager.common.exception;

public class ResourceNotFoundException extends RuntimeException {

	public ResourceNotFoundException(String message) {
		super(message);
	}

	public ResourceNotFoundException(String resourceName, Object identifier) {
		super("%s not found: %s".formatted(resourceName, identifier));
	}
}
