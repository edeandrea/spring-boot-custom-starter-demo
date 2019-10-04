package com.mycompany.myframework.service.security.servlet;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import com.mycompany.myframework.properties.config.MyFrameworkConfig.SecurityConfig.CorsConfig;
import com.mycompany.myframework.service.security.AllowedDomainsCorsConfigurationSourceBase;

/**
 * {@link CorsConfigurationSource} which will allow specification of domains and/or subdomains in {@link CorsConfiguration#getAllowedOrigins()}.
 *
 * @author Eric Deandrea
 */
public class AllowedDomainsCorsConfigurationSource extends AllowedDomainsCorsConfigurationSourceBase implements CorsConfigurationSource {
	@Nullable
	private final CorsConfigurationSource delegate;

	public AllowedDomainsCorsConfigurationSource(@Nullable CorsConfigurationSource delegate, CorsConfig corsConfig) {
		super(corsConfig);
		this.delegate = delegate;
	}

	@Nullable
	@Override
	public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
		return addOriginDomainIfApplicable(
			request.getHeader(HttpHeaders.ORIGIN),
			Optional.ofNullable(this.delegate)
				.map(theDelegate -> theDelegate.getCorsConfiguration(request))
				.orElse(null)
		);
	}
}
