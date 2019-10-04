package com.mycompany.myframework.service.security.server;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.util.Assert;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.ServerWebExchange;

import com.mycompany.myframework.properties.config.MyFrameworkConfig.SecurityConfig.CorsConfig;
import com.mycompany.myframework.service.security.AllowedDomainsCorsConfigurationSourceTestsBase;

public class ServerAllowedDomainsCorsProcessorTests extends AllowedDomainsCorsConfigurationSourceTestsBase {
	@Override
	@BeforeEach
	public void initialize() {
		super.initialize();
	}

	@Test
	public void noCorsConfigurationSourceSet() {
		assertThatIllegalArgumentException()
			.isThrownBy(() -> new ServerAllowedDomainsCorsProcessor(null));
	}

	@ParameterizedTest(name = "allowed [{index}] {arguments}")
	@MethodSource("allowedOrigins")
	public void allowed(String origin) {
		ServerWebExchange exchange = createExchange(origin);

		assertThat(isCorsAccepted(exchange))
			.isTrue();

		assertThat(exchange.getResponse())
			.extracting(
				res -> res.getHeaders().getVary(),
				res -> res.getHeaders().getAccessControlAllowOrigin(),
				res -> res.getHeaders().getAccessControlAllowMethods(),
				res -> res.getHeaders().getAccessControlAllowHeaders(),
				res -> res.getHeaders().getAccessControlAllowCredentials(),
				res -> res.getHeaders().getAccessControlMaxAge()
			)
			.containsExactly(
				Arrays.asList(HttpHeaders.ORIGIN, HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS),
				origin,
				Arrays.asList(HttpMethod.GET, HttpMethod.HEAD, HttpMethod.POST, HttpMethod.PUT),
				Arrays.asList(HttpHeaders.ACCEPT, HttpHeaders.CONTENT_TYPE),
				true,
				3600L
			);
	}

	@ParameterizedTest(name = "disallowed [{index}] {arguments}")
	@MethodSource("disallowedOrigins")
	public void disallowed(String origin) {
		ServerWebExchange exchange = createExchange(origin);

		assertThat(isCorsAccepted(exchange))
			.isFalse();

		assertThat(exchange.getResponse())
			.extracting(
				ServerHttpResponse::getStatusCode,
				res -> res.getHeaders().getVary(),
				res -> res.getHeaders().getAccessControlAllowOrigin(),
				res -> res.getHeaders().getAccessControlAllowMethods(),
				res -> res.getHeaders().getAccessControlAllowHeaders(),
				res -> res.getHeaders().getAccessControlAllowCredentials(),
				res -> res.getHeaders().getAccessControlMaxAge()
			)
			.containsExactly(
				HttpStatus.FORBIDDEN,
				Arrays.asList(HttpHeaders.ORIGIN, HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS),
				null,
				Collections.emptyList(),
				Collections.emptyList(),
				false,
				-1L
			);
	}

	@ParameterizedTest(name = "noAllowedDomainsConfigured [{index}] {arguments}")
	@MethodSource("allOrigins")
	public void noAllowedDomainsConfigured(String origin) {
		ServerWebExchange exchange = createExchange(origin);

		assertThat(isCorsAccepted(exchange, new CorsConfig()))
			.isTrue();

		assertThat(exchange.getResponse())
			.extracting(
				res -> res.getHeaders().getVary(),
				res -> res.getHeaders().getAccessControlAllowOrigin(),
				res -> res.getHeaders().getAccessControlAllowMethods(),
				res -> res.getHeaders().getAccessControlAllowHeaders(),
				res -> res.getHeaders().getAccessControlAllowCredentials(),
				res -> res.getHeaders().getAccessControlMaxAge()
			)
			.containsExactly(
				Arrays.asList(HttpHeaders.ORIGIN, HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS),
				origin,
				Arrays.asList(HttpMethod.GET, HttpMethod.HEAD, HttpMethod.POST, HttpMethod.PUT),
				Arrays.asList(HttpHeaders.ACCEPT, HttpHeaders.CONTENT_TYPE),
				true,
				3600L
			);
	}

	private ServerAllowedDomainsCorsProcessor createProcessor(CorsConfig corsConfig) {
		return new ServerAllowedDomainsCorsProcessor(new ServerAllowedDomainsCorsConfigurationSource(corsConfig));
	}

	private boolean isCorsAccepted(ServerWebExchange exchange) {
		return isCorsAccepted(exchange, this.corsConfig);
	}

	private boolean isCorsAccepted(ServerWebExchange exchange, CorsConfig corsConfig) {
		Assert.state(CorsUtils.isCorsRequest(exchange.getRequest()), "Must be a CORS request");
		return createProcessor(corsConfig).process(getCorsConfiguration(), exchange);
	}

	private static ServerWebExchange createExchange(String origin) {
		return MockServerWebExchange.from(
			MockServerHttpRequest.options("http://localhost/uri")
				.header(HttpHeaders.ORIGIN, origin)
				.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.PUT.name())
				.header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, HttpHeaders.ACCEPT, HttpHeaders.CONTENT_TYPE)
				.build()
		);
	}
}
