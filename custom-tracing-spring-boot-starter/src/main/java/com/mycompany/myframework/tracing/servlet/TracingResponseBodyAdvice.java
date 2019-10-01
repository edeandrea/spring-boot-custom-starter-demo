package com.mycompany.myframework.tracing.servlet;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.mycompany.myframework.tracing.TracingResponseModifierUtils;

/**
 * Adds tracing headers to the response. Clients can use them to lookup correlated logs or, if exported, lookup traces in zipkin
 */
@RestControllerAdvice
public class TracingResponseBodyAdvice implements ResponseBodyAdvice<Object> {
	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		return true;
	}

	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
		TracingResponseModifierUtils.addTracingHeaders(response);
		return body;
	}
}
