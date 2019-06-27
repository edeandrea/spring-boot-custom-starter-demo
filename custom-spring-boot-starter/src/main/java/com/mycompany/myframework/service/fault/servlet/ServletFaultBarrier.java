package com.mycompany.myframework.service.fault.servlet;

import java.util.Optional;
import java.util.UUID;

import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.TypeMismatchException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentConversionNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.mycompany.myframework.service.api.ValidationResponse;
import com.mycompany.myframework.service.api.servlet.ServletValidationResponse;
import com.mycompany.myframework.service.model.FaultException;
import com.mycompany.myframework.service.model.HttpStatusException;

@Order(Ordered.LOWEST_PRECEDENCE)
@RestControllerAdvice
public class ServletFaultBarrier extends ResponseEntityExceptionHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(ServletFaultBarrier.class);

	private ResponseEntity<Object> handleFaultException(FaultException ex, @Nullable HttpHeaders headers) {
		String faultId = ex.getFaultId();
		LOGGER.error("FaultException faultId={}: {}", faultId, ex.getMessage(), ex);

		ResponseEntity<Object> response = ResponseEntity.status(ex.getHttpStatus())
			.headers(createHeadersWithFaultId(headers, faultId))
			.body(ex.getResponseObject());

		return response;
	}

	@ExceptionHandler(FaultException.class)
	public ResponseEntity<Object> handleFaultException(FaultException ex) {
		return handleFaultException(ex, new HttpHeaders());
	}

	@ExceptionHandler(ConstraintViolationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ValidationResponse handleConstraintViolationException(ConstraintViolationException ex) {
		return new ValidationResponse(ex);
	}

	@Override
	protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
		BodyBuilder builder = ResponseEntity.status(status).headers(createHeadersWithFaultId(headers));

		if (ex instanceof MethodArgumentTypeMismatchException) {
			return builder.body(new ValidationResponse((MethodArgumentTypeMismatchException) ex));
		}
		else if (ex instanceof MethodArgumentConversionNotSupportedException) {
			return builder.body(new ValidationResponse((MethodArgumentConversionNotSupportedException) ex));
		}
		else {
			return builder.body(new ValidationResponse(ex));
		}
	}

	@Override
	protected ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
		return ResponseEntity.status(status).headers(createHeadersWithFaultId(headers)).body(new ValidationResponse(ex));
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
		return ResponseEntity.status(status).headers(createHeadersWithFaultId(headers)).body(new ValidationResponse(ex));
	}

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
		Throwable cause = ex.getCause();

		if (cause instanceof InvalidFormatException) {
			return ResponseEntity.status(status).headers(createHeadersWithFaultId(headers)).body(new ValidationResponse((InvalidFormatException) cause));
		}
		else if (cause instanceof UnrecognizedPropertyException) {
			return ResponseEntity.status(status).headers(createHeadersWithFaultId(headers)).body(new ValidationResponse((UnrecognizedPropertyException) cause));
		}
		else if (cause instanceof JsonMappingException) {
			return ResponseEntity.status(status).headers(createHeadersWithFaultId(headers)).body(new ValidationResponse((JsonMappingException) cause));
		}
		else if (ex.getMessage() != null && ex.getMessage().contains("Required request body is missing")) {
			return ResponseEntity.status(status).headers(createHeadersWithFaultId(headers)).body(new ValidationResponse(ex));
		}
		else {
			LOGGER.info("conversion error", ex);
			return handleFaultException(new FaultException(status, String.format("Got %s: %s", ex.getClass().getName(), ex.getMessage()), ex), headers);
		}
	}

	@Override
	protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
		return ResponseEntity
			.status(status)
			.headers(createHeadersWithFaultId(headers))
			.body(new ServletValidationResponse(ex));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> defaultErrorHandler(Exception ex) throws Exception {
		ResponseStatus responseStatus = AnnotationUtils.findAnnotation(ex.getClass(), ResponseStatus.class);

		if (responseStatus != null) {
			// If the exception is annotated with @ResponseStatus, rethrow it and let the framework handle it
			throw ex;
		}
		else {
			return handleFaultException(
				new FaultException(HttpStatus.INTERNAL_SERVER_ERROR, String.format("Exception of type %s occurred", ex.getClass().getName()), ex.getMessage(), ex)
					.responseObject(
						Optional.of(ex)
							.filter(HttpStatusException.class::isInstance)
							.map(HttpStatusException.class::cast)
							.map(HttpStatusException::getResponseObject)
							.orElse(null)
					)
			);
		}
	}

	private static HttpHeaders createHeadersWithFaultId(@Nullable HttpHeaders headers) {
		return createHeadersWithFaultId(headers, UUID.randomUUID().toString());
	}

	private static HttpHeaders createHeadersWithFaultId(@Nullable HttpHeaders headers, String faultId) {
		HttpHeaders newHeaders = new HttpHeaders();
		Optional.ofNullable(headers).ifPresent(newHeaders::addAll);
		newHeaders.add("X-FAULT-ID", faultId);

		return newHeaders;
	}
}
