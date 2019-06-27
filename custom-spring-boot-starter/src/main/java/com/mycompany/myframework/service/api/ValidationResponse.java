package com.mycompany.myframework.service.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.ConstraintViolationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import org.springframework.beans.TypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentConversionNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Validation errors")
@JacksonXmlRootElement(localName = "ValidationResponse")
public class ValidationResponse extends GenericResponse {
	@ApiModelProperty("The list of validation errors")
	@JacksonXmlElementWrapper(localName = "errors")
	@JacksonXmlProperty(localName = "error")
	private final List<Error> errors = new ArrayList<>();

	@ApiModelProperty("The developer message")
	@JacksonXmlCData
	@Nullable
	private String developerMessage;

	@SafeVarargs
	protected ValidationResponse(Status status, String developerMessage, Error... errors) {
		super(status);

		this.developerMessage = developerMessage;
		this.errors.addAll(
			Optional.ofNullable(errors)
				.map(Arrays::asList)
				.map(List::stream)
				.orElseGet(Stream::empty)
				.filter(Objects::nonNull)
				.collect(Collectors.toList())
		);
	}

	public ValidationResponse(MethodArgumentNotValidException ex) {
		this(ex.getBindingResult());
		this.developerMessage = ex.getMessage();
	}

	public ValidationResponse(BindingResult bindingResult) {
		super(Status.VALIDATION);

		this.errors.addAll(
			Optional.ofNullable(bindingResult.getAllErrors())
				.map(List::stream)
				.orElseGet(Stream::empty)
				.filter(Objects::nonNull)
				.map(error -> {
					Assert.state(error.getCode() != null, "error code is null but is expected to be not null to resolve message codes");

					if (error instanceof FieldError) {
						String[] messageCodes = bindingResult.resolveMessageCodes(error.getCode(), ((FieldError) error).getField());
						return new Error(((FieldError) error).getField(), error.getCode(), getLast(messageCodes, error.getCode(), error.getDefaultMessage()));
					}
					else {
						String[] messageCodes = bindingResult.resolveMessageCodes(error.getCode());
						return new Error(null, error.getCode(), getLast(messageCodes, error.getCode(), error.getDefaultMessage()));
					}
				})
				.collect(Collectors.toList())
		);
	}

	public ValidationResponse(InvalidFormatException ex) {
		super(Status.VALIDATION);
		this.developerMessage = ex.getMessage();
		this.errors.add(new Error(getErrorTextFromException(ex), "invalid"));
	}

	public ValidationResponse(UnrecognizedPropertyException ex) {
		super(Status.VALIDATION);
		this.developerMessage = ex.getMessage();
		this.errors.add(new Error(getErrorTextFromException(ex), "unexpected"));
	}

	public ValidationResponse(JsonMappingException ex) {
		super(Status.VALIDATION);
		this.developerMessage = ex.getMessage();
		this.errors.add(new Error(getErrorTextFromException(ex), "invalid"));
	}

	public ValidationResponse(HttpMessageNotReadableException ex) {
		super(Status.VALIDATION);
		this.developerMessage = ex.getMessage();
	}

	public ValidationResponse(ConstraintViolationException ex) {
		super(Status.VALIDATION);

		this.developerMessage = ex.getMessage();
		this.errors.addAll(
			Optional.ofNullable(ex.getConstraintViolations())
				.map(Set::stream)
				.orElseGet(Stream::empty)
				.map(violation -> new Error(violation.getPropertyPath().toString(), null, violation.getMessage()))
				.collect(Collectors.toList())
		);
	}

	public ValidationResponse(BindException ex) {
		this((BindingResult) ex);
		this.developerMessage = ex.getMessage();
	}

	public ValidationResponse(TypeMismatchException ex) {
		super(Status.ERROR);
		this.developerMessage = ex.getMessage();
		this.errors.add(new Error("", ex.getErrorCode(), ex.getMessage()));
	}

	public ValidationResponse(MethodArgumentTypeMismatchException ex) {
		super(Status.ERROR);
		this.developerMessage = ex.getMessage();
		this.errors.add(new Error(ex.getName(), ex.getErrorCode(), ex.getMessage()));
	}

	public ValidationResponse(MethodArgumentConversionNotSupportedException ex) {
		super(Status.ERROR);
		this.developerMessage = ex.getMessage();
		this.errors.add(new Error(ex.getName(), ex.getErrorCode(), ex.getMessage()));
	}

	@Nullable
	private static String getLast(@Nullable String[] strings, @Nullable String code, @Nullable String defaultIfEmpty) {
		return Optional.ofNullable(strings)
			.map(Arrays::asList)
			.map(List::stream)
			.orElseGet(Stream::empty)
			.reduce((first, second) -> second)
			.map(last -> StringUtils.equals(code, last) ? null : last)
			.orElse(defaultIfEmpty);
	}

	private static <T extends JsonMappingException> String getErrorTextFromException(@Nullable T ex) {
		StringBuilder sb = new StringBuilder();
		Optional<T> exOptional = Optional.ofNullable(ex);
		String exceptionClassName = exOptional.map(T::getClass).map(Class::getName).orElse(JsonMappingException.class.getName());

		exOptional
			.map(T::getPath)
			.orElseGet(Collections::emptyList)
			.stream()
			.filter(Objects::nonNull)
			.map(Reference::getFieldName)
			.filter(StringUtils::isNotBlank)
			.forEach(fieldName -> sb.append(fieldName).append("."));

		return (sb.length() > 0) ? sb.substring(0, sb.length() - 1) : String.format("No path found on exception %s: %s", exceptionClassName, exOptional.map(T::getMessage).orElse(exceptionClassName));
	}

	public List<Error> getErrors() {
		return Collections.unmodifiableList(this.errors);
	}

	@Nullable
	public String getDeveloperMessage() {
		return this.developerMessage;
	}

	protected void setDeveloperMessage(@Nullable String developerMessage) {
		this.developerMessage = developerMessage;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	@ApiModel(description = "A validation error")
	public static class Error {
		@ApiModelProperty("The field on the incoming payload containing the validation error")
		@Nullable
		private final String field;

		@ApiModelProperty("The code for the validation error")
		@Nullable
		private final String code;

		@ApiModelProperty("The message for the validation error")
		@JacksonXmlCData
		@Nullable
		private final String message;

		public Error(String field, String code) {
			this(field, code, null);
		}

		public Error(@Nullable String field, @Nullable String code, @Nullable String message) {
			super();
			this.field = field;
			this.code = code;
			this.message = message;
		}

		@Nullable
		public String getField() {
			return this.field;
		}

		@Nullable
		public String getCode() {
			return this.code;
		}

		@Nullable
		public String getMessage() {
			return this.message;
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}
	}
}
