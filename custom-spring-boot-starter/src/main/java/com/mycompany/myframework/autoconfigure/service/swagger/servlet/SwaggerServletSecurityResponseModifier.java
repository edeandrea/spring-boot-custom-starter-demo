package com.mycompany.myframework.autoconfigure.service.swagger.servlet;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import springfox.documentation.swagger.web.SecurityConfiguration;

/**
 * {@link ResponseBodyAdvice} applied to SpringFox responses when security is turned on in an application.
 * <p>
 * More specifically, this intercepts the securityConfiguration call that swagger-ui makes and injects the CSRF token in
 *  so that protected operations (PUT/PATCH/POST/etc) will function correctly within the swagger-ui.
 * </p>
 *
 * @author Eric Deandrea
 */
@RestControllerAdvice("springfox.documentation.swagger.web")
public class SwaggerServletSecurityResponseModifier implements ResponseBodyAdvice<SecurityConfiguration> {
	private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerServletSecurityResponseModifier.class);

	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		boolean supports = false;
		Type parameterType = returnType.getGenericParameterType();

		if (parameterType instanceof ParameterizedType) {
			LOGGER.debug("Return type is a parameterized type");
			ParameterizedType parameterizedType = (ParameterizedType) parameterType;

			if (parameterizedType.getRawType() == SecurityConfiguration.class) {
				LOGGER.debug("Directly returning {}", SecurityConfiguration.class.getName());
				supports = true;
			}
			else if (parameterizedType.getRawType() == ResponseEntity.class) {
				Type[] typeArguments = parameterizedType.getActualTypeArguments();

				if ((typeArguments != null) && (typeArguments.length == 1) && (typeArguments[0] == SecurityConfiguration.class)) {
					LOGGER.debug("Return type is a {} with a generic parameter of {}", ResponseEntity.class.getName(), SecurityConfiguration.class.getName());
					supports = true;
				}
			}
		}
		else {
			LOGGER.debug("Checking to see if the name of the method producing the configuration is securityConfiguration");
			Method method = returnType.getMethod();
			Assert.state(method != null, "wrapped method does not exist");
			supports = StringUtils.equals(method.getName(), "securityConfiguration");
		}

		return supports;
	}

	@Nullable
	@Override
	public SecurityConfiguration beforeBodyWrite(@Nullable SecurityConfiguration body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
		// See if current response is of type SecurityConfiguration
		LOGGER.debug("{}: Inside beforeBodyWrite for body of type {}", getClass().getName(), SecurityConfiguration.class.getName());

		ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		Assert.state(servletRequestAttributes!=null, "servletRequestAttributes is null");
		HttpServletRequest httpRequest = servletRequestAttributes.getRequest();

		Optional.ofNullable(httpRequest.getAttribute(CsrfToken.class.getName()))
			.filter(CsrfToken.class::isInstance)
			.map(CsrfToken.class::cast)
			.ifPresent(csrfToken -> addCsrfTokenToSecurityConfiguration(body, csrfToken.getToken(), csrfToken.getHeaderName()));

		return body;
	}

	private static void addCsrfTokenToSecurityConfiguration(@Nullable SecurityConfiguration securityConfiguration, @Nullable String csrfToken, @Nullable String csrfHeaderName) {
		if (StringUtils.isNotBlank(csrfToken) && (securityConfiguration != null) && StringUtils.equalsIgnoreCase(securityConfiguration.getApiKeyName(), csrfHeaderName)) {
			// Since we need to change value of field apiKey which is not accessible use reflection
			LOGGER.debug("Found csrf token and config.apiKeyName == {}", csrfHeaderName);

			try {
				Field field = ReflectionUtils.findField(SecurityConfiguration.class, "apiKey");
				Assert.state(field != null, "field is null");
				ReflectionUtils.makeAccessible(field);
				ReflectionUtils.setField(field, securityConfiguration, csrfToken);
			}
			catch (Exception ex) {
				LOGGER.info("Failed to add CSRF token on Swagger", ex);
			}
		}
	}
}
