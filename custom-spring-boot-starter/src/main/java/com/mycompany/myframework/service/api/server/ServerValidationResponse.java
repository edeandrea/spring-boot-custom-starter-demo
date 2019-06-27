package com.mycompany.myframework.service.api.server;

import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;

import com.mycompany.myframework.service.api.Status;
import com.mycompany.myframework.service.api.ValidationResponse;

public class ServerValidationResponse extends ValidationResponse {
	public ServerValidationResponse(WebExchangeBindException ex) {
		super(ex.getBindingResult());
		setDeveloperMessage(ex.getMessage());
	}

	public ServerValidationResponse(ServerWebInputException ex) {
		super(Status.VALIDATION, ex.getMessage());
	}
}
