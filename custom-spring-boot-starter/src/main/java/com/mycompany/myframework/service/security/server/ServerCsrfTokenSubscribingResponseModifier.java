package com.mycompany.myframework.service.security.server;

import java.util.List;
import java.util.Optional;

import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.web.reactive.HandlerResult;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import org.springframework.web.reactive.result.method.annotation.ResponseBodyResultHandler;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * {@link ResponseBodyResultHandler} for subscribing to the csrf token within a REST API
 *
 * @author Eric Deandrea
 */
public class ServerCsrfTokenSubscribingResponseModifier extends ResponseBodyResultHandler {
	public ServerCsrfTokenSubscribingResponseModifier(List<HttpMessageWriter<?>> writers, RequestedContentTypeResolver resolver, ReactiveAdapterRegistry registry) {
		super(writers, resolver, registry);
		setOrder(99);
	}

	@Override
	public Mono<Void> handleResult(ServerWebExchange exchange, HandlerResult result) {
		return Optional.ofNullable(exchange.getAttribute(CsrfToken.class.getName()))
			.filter(Mono.class::isInstance)
			.map(Mono.class::cast)
			.orElseGet(Mono::empty)
			.then(super.handleResult(exchange, result));
	}
}
