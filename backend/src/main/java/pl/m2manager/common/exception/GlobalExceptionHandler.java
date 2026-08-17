package pl.m2manager.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(
			MethodArgumentNotValidException ex,
			HttpServletRequest request
	) {
		List<ErrorResponse.FieldErrorDetail> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> new ErrorResponse.FieldErrorDetail(error.getField(), error.getDefaultMessage()))
				.toList();

		return buildResponse(
				HttpStatus.BAD_REQUEST,
				"Validation failed",
				request.getRequestURI(),
				fieldErrors
		);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorResponse> handleConstraintViolation(
			ConstraintViolationException ex,
			HttpServletRequest request
	) {
		List<ErrorResponse.FieldErrorDetail> fieldErrors = ex.getConstraintViolations().stream()
				.map(violation -> new ErrorResponse.FieldErrorDetail(
						violation.getPropertyPath().toString(),
						violation.getMessage()
				))
				.toList();

		return buildResponse(
				HttpStatus.BAD_REQUEST,
				"Validation failed",
				request.getRequestURI(),
				fieldErrors
		);
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleBadCredentials(
			BadCredentialsException ex,
			HttpServletRequest request
	) {
		return buildResponse(
				HttpStatus.UNAUTHORIZED,
				ex.getMessage(),
				request.getRequestURI(),
				null
		);
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleResourceNotFound(
			ResourceNotFoundException ex,
			HttpServletRequest request
	) {
		return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI(), null);
	}

	@ExceptionHandler(BusinessConflictException.class)
	public ResponseEntity<ErrorResponse> handleBusinessConflict(
			BusinessConflictException ex,
			HttpServletRequest request
	) {
		return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI(), null);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgument(
			IllegalArgumentException ex,
			HttpServletRequest request
	) {
		return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), null);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGeneralException(
			HttpServletRequest request
	) {
		return buildResponse(
				HttpStatus.INTERNAL_SERVER_ERROR,
				"An unexpected error occurred",
				request.getRequestURI(),
				null
		);
	}

	private ResponseEntity<ErrorResponse> buildResponse(
			HttpStatus status,
			String message,
			String path,
			List<ErrorResponse.FieldErrorDetail> fieldErrors
	) {
		ErrorResponse body = new ErrorResponse(
				Instant.now(),
				status.value(),
				status.getReasonPhrase(),
				message,
				path,
				fieldErrors
		);
		return ResponseEntity.status(status).body(body);
	}
}
