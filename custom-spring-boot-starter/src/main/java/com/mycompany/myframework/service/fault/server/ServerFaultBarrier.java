package com.mycompany.myframework.service.fault.server;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.collections4.CollectionUtils;

import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.function.server.ServerResponse.Context;
import org.springframework.web.reactive.handler.WebFluxResponseStatusExceptionHandler;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class ServerFaultBarrier extends WebFluxResponseStatusExceptionHandler implements Ordered {
	private static final Logger LOGGER = LoggerFactory.getLogger(ServerFaultBarrier.class);
	private final DefaultServerFaultBarrierResponseResolver responseResolver = new DefaultServerFaultBarrierResponseResolver();

	private Mono<ServerResponse> determineServerResponse(Throwable ex) {
		try {
			return this.responseResolver.handleFault(ex);
		}
		catch (Exception resolverException) {
			LOGGER.debug("Got exception {} while applying resolver {}: {}", resolverException.getClass().getName(), this.responseResolver.getClass().getName(), resolverException.getMessage(), resolverException);
			throw resolverException;
		}
	}

	/**
	 * Gets the {@link MediaType} to be used as the {@code Content-Type} of the response
	 * @param response The {@link ServerResponse}
	 * @param exchange The {@link ServerWebExchange}
	 * @return The {@link MediaType} to be used as the {@code Content-Type} of the response
	 */
	@Nullable
	private MediaType getResponseContentType(ServerResponse response, ServerWebExchange exchange) {
		// 1) Try to read it directly from the ResponseEntity
		// 2) Try to read it from the response on the exchange
		// 3) Try to see if the incoming request had at least 1 Accept header, if so use the 1st one
		// 4) Otherwise leave it alone and let the framework try & decide what to do
		return Optional.of(response)
			.map(ServerResponse::headers)
			.map(HttpHeaders::getContentType)
			.orElseGet(() -> Optional.ofNullable(exchange.getResponse().getHeaders().getContentType())
				.orElseGet(() ->
					Optional.ofNullable(exchange.getRequest().getHeaders().getAccept())
						.filter(CollectionUtils::isNotEmpty)
						.map(acceptHeaders -> acceptHeaders.get(0))
						.orElse(null)
				)
			);
	}

	@Override
	public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
		LOGGER.info("{}: Handling fault: {}", getClass().getSimpleName(), ex.toString());

		return determineServerResponse(ex)
			.flatMap(response -> {
				LOGGER.debug("Got response = {} for fault of type {}", response, ex.getClass().getName());

				if (exchange.getResponse().setStatusCode(response.statusCode())) {
					exchange.getResponse().getHeaders().setContentType(getResponseContentType(response, exchange));
					return response.writeTo(exchange, new HandlerStrategiesResponseContext(HandlerStrategies.withDefaults()));
				}
				else {
					return Mono.error(ex);
				}
			});
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

	private static final class HandlerStrategiesResponseContext implements Context {
		private final HandlerStrategies strategies;

		private HandlerStrategiesResponseContext(HandlerStrategies strategies) {
			this.strategies = strategies;
		}

		@Override
		public List<HttpMessageWriter<?>> messageWriters() {
			return this.strategies.messageWriters();
		}

		@Override
		public List<ViewResolver> viewResolvers() {
			return this.strategies.viewResolvers();
		}
	}
}
