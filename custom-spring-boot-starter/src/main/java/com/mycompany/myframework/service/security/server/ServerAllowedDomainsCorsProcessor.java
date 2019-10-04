package com.mycompany.myframework.service.security.server;

import java.util.Optional;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsProcessor;
import org.springframework.web.cors.reactive.DefaultCorsProcessor;
import org.springframework.web.server.ServerWebExchange;

/**
 * {@link CorsProcessor} for handling {@link ServerAllowedDomainsCorsConfigurationSource allowed domains CORS configuration}
 *
 * @author Eric Deandrea
 */
public class ServerAllowedDomainsCorsProcessor implements CorsProcessor {
	private final CorsProcessor underlyingProcessor;
	private final ServerAllowedDomainsCorsConfigurationSource corsConfigurationSource;

	public ServerAllowedDomainsCorsProcessor(ServerAllowedDomainsCorsConfigurationSource corsConfigurationSource) {
		this(null, corsConfigurationSource);
	}

	public ServerAllowedDomainsCorsProcessor(@Nullable CorsProcessor underlyingProcessor, ServerAllowedDomainsCorsConfigurationSource corsConfigurationSource) {
		Assert.notNull(corsConfigurationSource, "corsConfigurationSource can not be null");
		this.underlyingProcessor = Optional.ofNullable(underlyingProcessor).orElseGet(DefaultCorsProcessor::new);
		this.corsConfigurationSource = corsConfigurationSource;
	}

	@Override
	public boolean process(@Nullable CorsConfiguration config, ServerWebExchange exchange) {
		return this.underlyingProcessor.process(this.corsConfigurationSource.getCorsConfiguration(exchange, config), exchange);
	}
}
