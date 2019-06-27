package com.mycompany.myframework.service.api.servlet;

import org.springframework.web.bind.MissingServletRequestParameterException;

import com.mycompany.myframework.service.api.Status;
import com.mycompany.myframework.service.api.ValidationResponse;

public class ServletValidationResponse extends ValidationResponse {
	public ServletValidationResponse(MissingServletRequestParameterException ex) {
		super(Status.VALIDATION, ex.getMessage(), new Error(ex.getParameterName(), "missing", ex.getMessage()));
	}
}
