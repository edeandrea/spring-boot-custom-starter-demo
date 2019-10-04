package com.mycompany.myframework.service.security.servlet;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.commons.lang3.exception.ExceptionUtils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.Assert;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsProcessor;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.DefaultCorsProcessor;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.mycompany.myframework.properties.config.MyFrameworkConfig.SecurityConfig.CorsConfig;
import com.mycompany.myframework.service.security.AllowedDomainsCorsConfigurationSourceTestsBase;

public class AllowedDomainsCorsConfigurationSourceTests extends AllowedDomainsCorsConfigurationSourceTestsBase {
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
		HttpServletRequest request = createRequest(origin);
		HttpServletResponse response = new MockHttpServletResponse();

		AllowedDomainsCorsConfigurationSource configurationSource = new AllowedDomainsCorsConfigurationSource(DELEGATE, this.corsConfig);

		assertThat(configurationSource.getCorsConfiguration(request))
			.isNotNull()
			.extracting(
				corsConfig -> isCorsAccepted(request, response, corsConfig),
				corsConfig -> corsConfig.getAllowedOrigins().contains(origin),
				corsConfig -> corsConfig.getAllowedOrigins().contains(CorsConfiguration.ALL)
			)
			.containsExactly(
				true,
				true,
				false
			);

		assertThat(response)
			.extracting(
				HttpServletResponse::getStatus,
				res -> res.getHeaders(HttpHeaders.VARY),
				res -> res.getHeaders(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN),
				res -> res.getHeaders(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS),
				res -> res.getHeaders(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS),
				res -> res.getHeaders(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS),
				res -> res.getHeaders(HttpHeaders.ACCESS_CONTROL_MAX_AGE)
			)
			.containsExactly(
				HttpStatus.OK.value(),
				Arrays.asList(HttpHeaders.ORIGIN, HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS),
				Arrays.asList(origin),
				Arrays.asList(Stream.of(HttpMethod.GET.name(), HttpMethod.HEAD.name(), HttpMethod.POST.name(), HttpMethod.PUT.name()).collect(Collectors.joining(","))),
				Arrays.asList(Stream.of(HttpHeaders.ACCEPT, HttpHeaders.CONTENT_TYPE).collect(Collectors.joining(", "))),
				Arrays.asList("true"),
				Arrays.asList("3600")
			);
	}

	@ParameterizedTest(name = "disallowed [{index}] {arguments}")
	@MethodSource("disallowedOrigins")
	public void disallowed(String origin) {
		HttpServletRequest request = createRequest(origin);
		HttpServletResponse response = new MockHttpServletResponse();

		AllowedDomainsCorsConfigurationSource configurationSource = new AllowedDomainsCorsConfigurationSource(DELEGATE, this.corsConfig);

		assertThat(configurationSource.getCorsConfiguration(request))
			.isNotNull()
			.extracting(
				corsConfig -> isCorsAccepted(request, response, corsConfig),
				corsConfig -> corsConfig.getAllowedOrigins().contains(origin),
				corsConfig -> corsConfig.getAllowedOrigins().contains(CorsConfiguration.ALL)
			)
			.containsExactly(
				false,
				false,
				false
			);

		assertThat(response)
			.extracting(
				HttpServletResponse::getStatus,
				res -> res.getHeaders(HttpHeaders.VARY),
				res -> res.getHeaders(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN),
				res -> res.getHeaders(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS),
				res -> res.getHeaders(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS),
				res -> res.getHeaders(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS),
				res -> res.getHeaders(HttpHeaders.ACCESS_CONTROL_MAX_AGE)
			)
			.containsExactly(
				HttpStatus.FORBIDDEN.value(),
				Arrays.asList(HttpHeaders.ORIGIN, HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS),
				Collections.emptyList(),
				Collections.emptyList(),
				Collections.emptyList(),
				Collections.emptyList(),
				Collections.emptyList()
			);
	}

	@ParameterizedTest(name = "all [{index}] {arguments}")
	@MethodSource("allOrigins")
	public void noAllowedDomainsConfigured(String origin) {
		HttpServletRequest request = createRequest(origin);
		HttpServletResponse response = new MockHttpServletResponse();

		AllowedDomainsCorsConfigurationSource configurationSource = new AllowedDomainsCorsConfigurationSource(DELEGATE, new CorsConfig());

		assertThat(configurationSource.getCorsConfiguration(request))
			.isNotNull()
			.extracting(
				corsConfig -> isCorsAccepted(request, response, corsConfig),
				corsConfig -> corsConfig.getAllowedOrigins().contains(origin),
				corsConfig -> corsConfig.getAllowedOrigins().contains(CorsConfiguration.ALL)
			)
			.containsExactly(
				true,
				false,
				true
			);

		assertThat(response)
			.extracting(
				HttpServletResponse::getStatus,
				res -> res.getHeaders(HttpHeaders.VARY),
				res -> res.getHeaders(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN),
				res -> res.getHeaders(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS),
				res -> res.getHeaders(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS),
				res -> res.getHeaders(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS),
				res -> res.getHeaders(HttpHeaders.ACCESS_CONTROL_MAX_AGE)
			)
			.containsExactly(
				HttpStatus.OK.value(),
				Arrays.asList(HttpHeaders.ORIGIN, HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS),
				Arrays.asList(origin),
				Arrays.asList(Stream.of(HttpMethod.GET.name(), HttpMethod.HEAD.name(), HttpMethod.POST.name(), HttpMethod.PUT.name()).collect(Collectors.joining(","))),
				Arrays.asList(Stream.of(HttpHeaders.ACCEPT, HttpHeaders.CONTENT_TYPE).collect(Collectors.joining(", "))),
				Arrays.asList("true"),
				Arrays.asList("3600")
			);
	}

	private static HttpServletRequest createRequest(String origin) {
		return MockMvcRequestBuilders.options("/uri")
			.header(HttpHeaders.ORIGIN, origin)
			.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.PUT.name())
			.header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, HttpHeaders.ACCEPT, HttpHeaders.CONTENT_TYPE)
			.buildRequest(new MockServletContext());
	}

	private static boolean isCorsAccepted(HttpServletRequest request, HttpServletResponse response, CorsConfiguration corsConfiguration) {
		Assert.state(CorsUtils.isCorsRequest(request), "Must be a CORS request");

		try {
			return PROCESSOR.processRequest(corsConfiguration, request, response);
		}
		catch (IOException ex) {
			return ExceptionUtils.rethrow(ex);
		}
	}
}
