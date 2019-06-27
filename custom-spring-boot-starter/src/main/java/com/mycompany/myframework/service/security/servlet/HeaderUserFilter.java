package com.mycompany.myframework.service.security.servlet;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;

public class HeaderUserFilter extends RequestHeaderAuthenticationFilter {
	private static final Logger LOGGER = LoggerFactory.getLogger(HeaderUserFilter.class);

	public HeaderUserFilter(AuthenticationManager authenticationManager, String principalHeaderName) {
		super();

		setAuthenticationManager(authenticationManager);
		setCheckForPrincipalChanges(true);
		setExceptionIfHeaderMissing(false);
		setPrincipalRequestHeader(principalHeaderName);
	}

	public HeaderUserFilter(AuthenticationManager authenticationManager, String principalHeaderName, @Nullable ApplicationEventPublisher applicationEventPublisher) {
		this(authenticationManager, principalHeaderName);
		setApplicationEventPublisher(applicationEventPublisher);
	}

	@Override
	protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
		try {
			return super.getPreAuthenticatedPrincipal(request);
		}
		catch (PreAuthenticatedCredentialsNotFoundException ex) {
			LOGGER.debug("Cleared security context - no principal header found in the request");
			SecurityContextHolder.clearContext();

			throw ex;
		}
	}
}
