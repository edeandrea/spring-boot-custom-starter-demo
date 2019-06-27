package com.mycompany.myframework.service.model;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Some exception that tracks an http status occurred")
@JsonIgnoreProperties({ "stackTrace", "suppressedExceptions", "cause" })
public abstract class HttpStatusException extends RuntimeException {
	@ApiModelProperty("The http status of the exception")
	private final HttpStatus httpStatus;

	@ApiModelProperty("The reason for the exception")
	@JacksonXmlCData
	@Nullable
	private final String reason;

	@ApiModelProperty("The response object")
	@Nullable
	private Object responseObject;

	protected HttpStatusException(HttpStatus httpStatus, String reason) {
		this(httpStatus, reason, null, null);
	}

	protected HttpStatusException(HttpStatus httpStatus, String reason, @Nullable String message, @Nullable Throwable cause) {
		super(message, cause);

		this.httpStatus = httpStatus;
		this.reason = StringUtils.isNotBlank(reason) ? reason : Optional.of(httpStatus).map(HttpStatus::getReasonPhrase).orElse(null);
	}

	protected HttpStatusException(HttpStatus httpStatus, String reason, String message) {
		this(httpStatus, reason, message, null);
	}

	protected HttpStatusException(HttpStatus httpStatus, String reason, Throwable cause) {
		this(httpStatus, reason, null, cause);
	}

	public final HttpStatus getHttpStatus() {
		return this.httpStatus;
	}

	@Nullable
	public final String getReason() {
		return this.reason;
	}

	@Nullable
	public Object getResponseObject() {
		return this.responseObject;
	}

	public void setResponseObject(@Nullable Object responseObject) {
		this.responseObject = responseObject;
	}

	public HttpStatusException responseObject(@Nullable Object responseObject) {
		setResponseObject(responseObject);
		return this;
	}

	@ApiModelProperty("The cause message of this fault")
	@JsonInclude(Include.NON_EMPTY)
	@JacksonXmlCData
	@Nullable
	public String getCauseMessage() {
		Throwable cause = getCause();

		return (cause != null) ? String.format("%s: %s", cause.getClass().getName(), cause.getMessage()) : null;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
