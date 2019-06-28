package com.mycompany.myframework.autoconfigure.actuator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import com.mycompany.myframework.autoconfigure.service.DefaultPropertySetter;

/**
 * Looks to see if the property <strong>mycompany.myframework.config.enable-actuators</strong> == <strong>true</strong>
 * <p>
 *   If so, sets the following properties:
 *   <ul>
 *     <li>management.endpoints.web.exposure.include=*</li>
 *     <li>management.endpoint.health.show-details=always</li>
 *   </ul>
 * </p>
 *
 * @author Eric Deandrea
 */
public class ActuatorsApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {
	private static final Logger LOGGER = LoggerFactory.getLogger(ActuatorsApplicationContextInitializer.class);
	private static final int DEFAULT_ORDER = Ordered.LOWEST_PRECEDENCE - 50;

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		ConfigurableEnvironment environment = applicationContext.getEnvironment();
		boolean shouldActuatorsBeEnabled = shouldActuatorsBeEnabled(environment);
		LOGGER.debug("shouldActuatorsBeEnabled? {}", shouldActuatorsBeEnabled);

		if (shouldActuatorsBeEnabled) {
			DefaultPropertySetter.addDefaultProperty(environment, "management.endpoints.web.exposure.include", "*");
			DefaultPropertySetter.addDefaultProperty(environment, "management.endpoint.health.show-details", "always");
		}
	}

	@Override
	public int getOrder() {
		return DEFAULT_ORDER;
	}

	private static boolean shouldActuatorsBeEnabled(ConfigurableEnvironment environment) {
		String key = "mycompany.myframework.config.enable-actuators";

		return (environment.containsProperty(key) && environment.getProperty(key, Boolean.class, false)) ||
			(environment.containsProperty(convertToUpperWithUnderscore(key))) && environment.getProperty(convertToUpperWithUnderscore(key), Boolean.class, false);
	}

	private static String convertToUpperWithUnderscore(String string) {
		return StringUtils.replace(StringUtils.upperCase(string), ".", "_");
	}
}
