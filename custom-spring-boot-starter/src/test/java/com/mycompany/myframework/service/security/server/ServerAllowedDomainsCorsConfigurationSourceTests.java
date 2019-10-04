package com.mycompany.myframework.service.security.server;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.util.Assert;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsProcessor;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.cors.reactive.DefaultCorsProcessor;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;

import com.mycompany.myframework.properties.config.MyFrameworkConfig.SecurityConfig.CorsConfig;
import com.mycompany.myframework.service.security.AllowedDomainsCorsConfigurationSourceTestsBase;

public class ServerAllowedDomainsCorsConfigurationSourceTests extends AllowedDomainsCorsConfigurationSourceTestsBase {
	private static final CorsProcessor PROCESSOR = new DefaultCorsProcessor();
	private static final UrlBasedCorsConfigurationSource DELEGATE = new UrlBasedCorsConfigurationSource();

	@Override
	@BeforeEach
	public void initialize() {
		super.initialize();
		DELEGATE.registerCorsConfiguration("/**", getCorsConfiguration());
	}

	@ParameterizedTest(name = "allowed [{index}] {arguments}")
	@MethodSource("allowedOrigins")
	public void allowed(String origin) {
		ServerWebExchange exchange = createExchange(origin);

		ServerAllowedDomainsCorsConfigurationSource configurationSource = new ServerAllowedDomainsCorsConfigurationSource(DELEGATE, this.corsConfig);
		CorsConfiguration corsConfiguration = configurationSource.getCorsConfiguration(exchange);

		assertThat(corsConfiguration)
			.isNotNull()
			.extracting(
				corsConfig -> isCorsAccepted(exchange, corsConfig),
				corsConfig -> corsConfig.getAllowedOrigins().contains(origin),
				corsConfig -> corsConfig.getAllowedOrigins().contains(CorsConfiguration.ALL)
			)
			.containsExactly(
				true,
				true,
				false
			);

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

		ServerAllowedDomainsCorsConfigurationSource configurationSource = new ServerAllowedDomainsCorsConfigurationSource(DELEGATE, this.corsConfig);
		CorsConfiguration corsConfiguration = configurationSource.getCorsConfiguration(exchange);

		assertThat(corsConfiguration)
			.isNotNull()
			.extracting(
				corsConfig -> isCorsAccepted(exchange, corsConfig),
				corsConfig -> corsConfig.getAllowedOrigins().contains(origin),
				corsConfig -> corsConfig.getAllowedOrigins().contains(CorsConfiguration.ALL)
			)
			.containsExactly(
				false,
				false,
				false
			);

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

		ServerAllowedDomainsCorsConfigurationSource configurationSource = new ServerAllowedDomainsCorsConfigurationSource(DELEGATE, new CorsConfig());
		CorsConfiguration corsConfiguration = configurationSource.getCorsConfiguration(exchange);

		assertThat(corsConfiguration)
			.isNotNull()
			.extracting(
				corsConfig -> isCorsAccepted(exchange, corsConfig),
				corsConfig -> corsConfig.getAllowedOrigins().contains(origin),
				corsConfig -> corsConfig.getAllowedOrigins().contains(CorsConfiguration.ALL)
			)
			.containsExactly(
				true,
				false,
				true
			);

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

	private static ServerWebExchange createExchange(String origin) {
		return MockServerWebExchange.from(
			MockServerHttpRequest.options("http://localhost/uri")
				.header(HttpHeaders.ORIGIN, origin)
				.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.PUT.name())
				.header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, HttpHeaders.ACCEPT, HttpHeaders.CONTENT_TYPE)
				.build()
		);
	}

	private static boolean isCorsAccepted(ServerWebExchange exchange, CorsConfiguration corsConfiguration) {
		Assert.state(CorsUtils.isCorsRequest(exchange.getRequest()), "Must be a CORS request");
		return PROCESSOR.process(corsConfiguration, exchange);
	}
}
