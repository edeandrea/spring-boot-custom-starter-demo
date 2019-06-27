package com.mycompany.myframework.service.api;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "The status of the response")
public enum Status {
	VALIDATION, OK, ERROR
}
