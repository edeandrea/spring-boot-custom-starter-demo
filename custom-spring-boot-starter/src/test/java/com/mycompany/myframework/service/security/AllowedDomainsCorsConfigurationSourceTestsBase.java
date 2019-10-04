package com.mycompany.myframework.service.security;

import java.util.stream.Stream;

import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;

import com.mycompany.myframework.properties.config.MyFrameworkConfig.SecurityConfig.CorsConfig;

public abstract class AllowedDomainsCorsConfigurationSourceTestsBase {
	protected final CorsConfig corsConfig = new CorsConfig();

	protected void initialize() {
		this.corsConfig.setAllowedDomains("redhat.com, subdomain1.redhat.net, subdomain2.redhat.net");
	}

	protected CorsConfiguration getCorsConfiguration() {
		CorsConfiguration corsConfiguration = new CorsConfiguration().applyPermitDefaultValues();
		corsConfiguration.addAllowedMethod(HttpMethod.PUT);
		corsConfiguration.setMaxAge(3600L);
		corsConfiguration.setAllowCredentials(true);

		return corsConfiguration;
	}

	protected static Stream<String> allOrigins() {
		return Stream.concat(allowedOrigins(), disallowedOrigins());
	}

	protected static Stream<String> allowedOrigins() {
		return Stream.of(
			"http://www.redhat.com",
			"http://somewhere.somehow.redhat.com",
			"https://somewhere.subdomain1.redhat.net",
			"http://subdomain2.redhat.net",
			"https://somewhere.somehow.someway.subdomain2.redhat.net"
		);
	}

	protected static Stream<String> disallowedOrigins() {
		return Stream.of(
			"https://www.redhat.net",
			"http://somewhere.someway.redhat.net",
			"https://subdomain3.redhat.net",
			"http://example.com",
			"https://example.com"
		);
	}
}
