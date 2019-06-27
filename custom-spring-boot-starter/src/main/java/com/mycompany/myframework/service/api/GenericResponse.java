package com.mycompany.myframework.service.api;

import org.apache.commons.lang3.builder.ToStringBuilder;

import org.springframework.lang.Nullable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "General-purpose response")
public class GenericResponse {
	@ApiModelProperty("The status")
	private final Status status;

	@ApiModelProperty("The payload for the response")
	@Nullable
	private final Object response;

	public GenericResponse(Status status, @Nullable Object response) {
		super();
		this.status = status;
		this.response = response;
	}

	public GenericResponse(Status status) {
		this(status, null);
	}

	public GenericResponse() {
		this(Status.OK);
	}

	public Status getStatus() {
		return this.status;
	}

	@Nullable
	public Object getResponse() {
		return this.response;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
