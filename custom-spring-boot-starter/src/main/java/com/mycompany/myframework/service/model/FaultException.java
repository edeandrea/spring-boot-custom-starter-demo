package com.mycompany.myframework.service.model;

import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;

import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;

@ApiModel(description = "Some Fault that occurred")
@JsonIgnoreProperties({ "stackTrace", "suppressedExceptions", "cause" })
public class FaultException extends HttpStatusException implements TraceableFault {
	@Nullable
	private String faultId = UUID.randomUUID().toString();

	public FaultException(HttpStatus httpStatus, String reason) {
		super(httpStatus, reason);
	}

	public FaultException(HttpStatus httpStatus, String reason, String message, Throwable cause) {
		super(httpStatus, reason, message, cause);
	}

	public FaultException(HttpStatus httpStatus, String reason, String message) {
		super(httpStatus, reason, message);
	}

	public FaultException(HttpStatus httpStatus, String reason, Throwable cause) {
		super(httpStatus, reason, cause);
	}

	@Override
	@Nullable
	public String getFaultId() {
		return this.faultId;
	}

	@Override
	public void setFaultId(@Nullable String faultId) {
		this.faultId = faultId;
	}

	public FaultException faultId(@Nullable String faultId) {
		setFaultId(faultId);
		return this;
	}

	@Override
	public FaultException responseObject(@Nullable Object responseObject) {
		return (FaultException) super.responseObject(responseObject);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
