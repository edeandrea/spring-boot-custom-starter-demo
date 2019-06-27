package com.mycompany.myframework.service.security.server;

import java.util.Optional;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * Pre-authenticated {@link ServerAuthenticationConverter} which obtains the principal name from a request header.
 * <p>
 *   As with most pre-authenticated scenarios, it is essential that the external authentication system is set up correctly. All the protection
 *   is assumed to be provided externally. If this is included inappropriately in a configuration, it would  poible to assume the identity
 *   of a user merely by setting the correct header name. This also means it should not generally be used in combination with other
 *   Spring Security authentication mechanisms (such as form login) as this would imply there was a means of bypassing the
 *   external system, which would be risky.
 * </p>
 * <p>
 *   This is essentially the reactive equivalent of {@link com.mycompany.myframework.service.security.servlet.HeaderUserFilter}, although the
 *   workflow on the reactive side is quite different.
 * </p>
 *
 * @author Eric Deandrea
 */
public class RequestHeaderServerAuthenticationConverter implements ServerAuthenticationConverter {
	/**
	 * The default value for the principal request header
	 */
	public static final String DEFAULT_PRINCIPAL_REQUEST_HEADER = "SM_USER";

	/**
	 * The default value for the credentials
	 */
	public static final String DEFAULT_CREDENTIALS = "N/A";

	private String principalRequestHeader = DEFAULT_PRINCIPAL_REQUEST_HEADER;
	private String defaultCredentials = DEFAULT_CREDENTIALS;

	@Nullable
	private String credentialsRequestHeader;

	@Override
	public Mono<Authentication> convert(ServerWebExchange exchange) {
		ServerHttpRequest request = exchange.getRequest();

		return Mono.justOrEmpty(request.getHeaders().getFirst(this.principalRequestHeader))
			.map(smUserHeader -> new PreAuthenticatedAuthenticationToken(smUserHeader, getCredentials(request)));
	}

	private String getCredentials(ServerHttpRequest request) {
		return Optional.ofNullable(this.credentialsRequestHeader)
			.map(request.getHeaders()::getFirst)
			.orElse(this.defaultCredentials);
	}

	/**
	 * Sets the header to look at for the principal
	 *
	 * @param principalRequestHeader The header to look at for the principal
	 */
	public void setPrincipalRequestHeader(String principalRequestHeader) {
		Assert.hasText(principalRequestHeader, "principalRequestHeader must not be empty or null");
		this.principalRequestHeader = principalRequestHeader;
	}

	/**
	 * Sets the header to look at for the credentials
	 *
	 * @param credentialsRequestHeader The header to look at for the credentials
	 */
	public void setCredentialsRequestHeader(String credentialsRequestHeader) {
		Assert.hasText(credentialsRequestHeader, "credentialsRequestHeader must not be empty or null");
		this.credentialsRequestHeader = credentialsRequestHeader;
	}

	/**
	 * Default credentials to use if none can be found
	 *
	 * @param defaultCredentials Default credentials to use if none can be found
	 */
	public void setDefaultCredentials(String defaultCredentials) {
		Assert.hasText(defaultCredentials, "defaultCredentials must not be empty or null");
		this.defaultCredentials = defaultCredentials;
	}
}
