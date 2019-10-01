package com.mycompany.myframework.tracing.reactive;

import java.util.List;

import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.web.reactive.HandlerResult;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import org.springframework.web.reactive.result.method.annotation.ResponseBodyResultHandler;
import org.springframework.web.server.ServerWebExchange;

import com.mycompany.myframework.tracing.TracingResponseModifierUtils;
import reactor.core.publisher.Mono;

public class TracingResponseBodyResultHandler extends ResponseBodyResultHandler {
	public TracingResponseBodyResultHandler(List<HttpMessageWriter<?>> writers, RequestedContentTypeResolver resolver, ReactiveAdapterRegistry registry) {
		super(writers, resolver, registry);
		setOrder(98);
	}

	@Override
	public Mono<Void> handleResult(ServerWebExchange exchange, HandlerResult result) {
		TracingResponseModifierUtils.addTracingHeaders(exchange.getResponse());
		return super.handleResult(exchange, result);
	}
}
