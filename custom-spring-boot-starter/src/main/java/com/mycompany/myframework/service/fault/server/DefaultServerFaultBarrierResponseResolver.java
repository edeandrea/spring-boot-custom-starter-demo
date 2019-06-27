package com.mycompany.myframework.service.fault.server;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.exception.ExceptionUtils;

import org.springframework.beans.TypeMismatchException;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.Assert;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.method.annotation.MethodArgumentConversionNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.mycompany.myframework.service.api.ValidationResponse;
import com.mycompany.myframework.service.api.server.ServerValidationResponse;
import com.mycompany.myframework.service.model.FaultException;
import com.mycompany.myframework.service.model.HttpStatusException;
import reactor.core.publisher.Mono;

public class DefaultServerFaultBarrierResponseResolver {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServerFaultBarrierResponseResolver.class);

	private Mono<ServerResponse> handleFaultException(FaultException ex) {
		return Mono.defer(() -> {
			String faultId = ex.getFaultId();
			LOGGER.error("FaultException faultId={}: {}", faultId, ex.getMessage(), ex);

			return ServerResponse
				.status(Optional.ofNullable(ex.getHttpStatus()).orElse(HttpStatus.INTERNAL_SERVER_ERROR))
				.headers(createHeadersWithFaultId(faultId))
				.syncBody(Optional.ofNullable(ex.getResponseObject()).orElse(ex));
		});
	}

	private ValidationResponse handleConstraintViolationException(ConstraintViolationException ex) {
		return new ValidationResponse(ex);
	}

	private Mono<ServerResponse> handleTypeMismatch(TypeMismatchException ex) {
		ValidationResponse body = null;

		if (ex instanceof MethodArgumentTypeMismatchException) {
			body = new ValidationResponse((MethodArgumentTypeMismatchException) ex);
		}
		else if (ex instanceof MethodArgumentConversionNotSupportedException) {
			body = new ValidationResponse((MethodArgumentConversionNotSupportedException) ex);
		}
		else {
			body = new ValidationResponse(ex);
		}

		return ServerResponse.badRequest().headers(createHeadersWithFaultId()).syncBody(body);
	}

	private Mono<ServerResponse> handleBindException(BindException ex) {
		return ServerResponse.badRequest().headers(createHeadersWithFaultId()).syncBody(new ValidationResponse(ex));
	}

	private Mono<ServerResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
		return ServerResponse.badRequest().headers(createHeadersWithFaultId()).syncBody(new ValidationResponse(ex));
	}

	private Mono<ServerResponse> handleServerWebInputException(ServerWebInputException ex) {
		return ServerResponse.status(ex.getStatus()).headers(createHeadersWithFaultId()).syncBody(new ServerValidationResponse(ex));
	}

	private Mono<ServerResponse> handleWebExchangeBindException(WebExchangeBindException ex) {
		return ServerResponse.status(ex.getStatus()).headers(createHeadersWithFaultId()).syncBody(new ServerValidationResponse(ex));
	}

	private Mono<ServerResponse> handleResponseStatusException(ResponseStatusException ex) {
		throw ex;
	}

	private Mono<ServerResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
		HttpStatus status = HttpStatus.BAD_REQUEST;
		Throwable cause = ex.getCause();

		if (cause instanceof InvalidFormatException) {
			return ServerResponse.status(status).headers(createHeadersWithFaultId()).syncBody(new ValidationResponse((InvalidFormatException) cause));
		}
		else if (cause instanceof UnrecognizedPropertyException) {
			return ServerResponse.status(status).headers(createHeadersWithFaultId()).syncBody(new ValidationResponse((UnrecognizedPropertyException) cause));
		}
		else if (cause instanceof JsonMappingException) {
			return ServerResponse.status(status).headers(createHeadersWithFaultId()).syncBody(new ValidationResponse((JsonMappingException) cause));
		}
		else if (ex.getMessage() != null && ex.getMessage().contains("Required request body is missing")) {
			return ServerResponse.status(status).headers(createHeadersWithFaultId()).syncBody(new ValidationResponse(ex));
		}
		else {
			LOGGER.info("conversion error", ex);
			return handleFaultException(new FaultException(status, String.format("Got %s: %s", ex.getClass().getName(), ex.getMessage()), ex));
		}
	}

	/**
	 * A default fault barrier which catches any other exceptions that haven't been handled.
	 * <p>
	 * If the exception itself is annotated with {@link ResponseStatus}, then that exception itself is re-thrown and
	 * Spring handles it accordingly. Otherwise the exception is treated as a {@link FaultException}.
	 * </p>
	 *
	 * @param ex The {@link Exception}
	 * @return The {@link ServerResponse}
	 * @throws Exception If the exception is annotated with {@link ResponseStatus}
	 * @see #handleFaultException(FaultException)
	 */
	private Mono<ServerResponse> defaultErrorHandler(Exception ex) throws Exception {
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

	public Mono<ServerResponse> handleFault(Throwable throwable) {
		Assert.notNull(throwable, "Exception must not be null");

		if (throwable instanceof FaultException) {
			return handleFaultException((FaultException) throwable);
		}
		else if (throwable instanceof ConstraintViolationException) {
			return ServerResponse.badRequest().headers(createHeadersWithFaultId()).syncBody(handleConstraintViolationException((ConstraintViolationException) throwable));
		}
		else if (throwable instanceof TypeMismatchException) {
			return handleTypeMismatch((TypeMismatchException) throwable);
		}
		else if (throwable instanceof BindException) {
			return handleBindException((BindException) throwable);
		}
		else if (throwable instanceof MethodArgumentNotValidException) {
			return handleMethodArgumentNotValidException((MethodArgumentNotValidException) throwable);
		}
		else if (throwable instanceof WebExchangeBindException) {
			return handleWebExchangeBindException((WebExchangeBindException) throwable);
		}
		else if (throwable instanceof ServerWebInputException) {
			return handleServerWebInputException((ServerWebInputException) throwable);
		}
		else if (throwable instanceof HttpMessageNotReadableException) {
			return handleHttpMessageNotReadable((HttpMessageNotReadableException) throwable);
		}
		else if (throwable instanceof ResponseStatusException) {
			return handleResponseStatusException((ResponseStatusException) throwable);
		}
		else if (throwable instanceof Exception) {
			try {
				return defaultErrorHandler((Exception) throwable);
			}
			catch (Exception e) {
				return ExceptionUtils.rethrow(e);
			}
		}
		else {
			return Mono.empty();
		}
	}

	private static Consumer<HttpHeaders> createHeadersWithFaultId() {
		return createHeadersWithFaultId(UUID.randomUUID().toString());
	}

	private static Consumer<HttpHeaders> createHeadersWithFaultId(String faultId) {
		return headers -> headers.add("X-FAULT-ID", faultId);
	}
}
