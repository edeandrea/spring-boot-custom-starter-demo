package com.mycompany.myframework.service.security.server;

import java.util.Optional;

import org.springframework.lang.Nullable;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;

import com.mycompany.myframework.properties.config.MyFrameworkConfig.SecurityConfig.CorsConfig;
import com.mycompany.myframework.service.security.AllowedDomainsCorsConfigurationSourceBase;

/**
 * {@link CorsConfigurationSource} which will allow specification of domains and/or subdomains in {@link CorsConfiguration#getAllowedOrigins()}.
 * <p>
 *   This is the reactive version of {@link com.mycompany.myframework.service.security.servlet.AllowedDomainsCorsConfigurationSource}.
 * </p>
 *
 * @author Eric Deandrea
 */
public class ServerAllowedDomainsCorsConfigurationSource extends AllowedDomainsCorsConfigurationSourceBase implements CorsConfigurationSource {
	@Nullable
	private final CorsConfigurationSource delegate;

	/**
	 * Constructs an instance
	 * @param delegate A delegate {@link CorsConfigurationSource}
	 * @param corsConfig The {@link CorsConfig}
	 */
	public ServerAllowedDomainsCorsConfigurationSource(@Nullable CorsConfigurationSource delegate, CorsConfig corsConfig) {
		super(corsConfig);
		this.delegate = delegate;
	}

	/**
	 * Constructs an instance
	 * @param corsConfig The {@link CorsConfig}
	 * @since 3.0
	 */
	public ServerAllowedDomainsCorsConfigurationSource(CorsConfig corsConfig) {
		this(null, corsConfig);
	}

	@Nullable
	@Override
	public CorsConfiguration getCorsConfiguration(ServerWebExchange exchange) {
		return getCorsConfiguration(exchange, null);
	}

	/**
	 * Return a {@link CorsConfiguration} based on the incoming request, merged with an existing {@link CorsConfiguration}
	 * @param exchange The {@link ServerWebExchange}
	 * @param corsConfiguration The existing {@link CorsConfiguration}
	 * @return The associated {@link CorsConfiguration}, or {@code null} if none
	 */
	@Nullable
	public CorsConfiguration getCorsConfiguration(ServerWebExchange exchange, @Nullable CorsConfiguration corsConfiguration) {
		return addOriginDomainIfApplicable(
			exchange.getRequest().getHeaders().getOrigin(),
			Optional.ofNullable(this.delegate)
				.map(theDelegate -> theDelegate.getCorsConfiguration(exchange))
				.orElse(corsConfiguration)
		);
	}
}
