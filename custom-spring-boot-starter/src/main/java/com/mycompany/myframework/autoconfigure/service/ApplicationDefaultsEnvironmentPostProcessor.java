package com.mycompany.myframework.autoconfigure.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Sets the following default properties
 * <p>
 *   <ul>
 *     <li>spring.jackson.default-property-inclusion=non_empty</li>
 *     <li>spring.jackson.deserialization.accept-empty-string-as-null-object=true</li>
 *     <li>spring.jackson.deserialization.accept-empty-array-as-null-object=true</li>
 *     <li>spring.jackson.deserialization.fail-on-null-for-primitives=true</li>
 *   </ul>
 * </p>
 *
 * @author Eric Deandrea
 */
public class ApplicationDefaultsEnvironmentPostProcessor implements EnvironmentPostProcessor {
	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		DefaultPropertySetter.addDefaultProperty(environment, "spring.jackson.default-property-inclusion", "non_empty");
		DefaultPropertySetter.addDefaultProperty(environment, "spring.jackson.deserialization.accept-empty-string-as-null-object", "true");
		DefaultPropertySetter.addDefaultProperty(environment, "spring.jackson.deserialization.accept-empty-array-as-null-object", "true");
		DefaultPropertySetter.addDefaultProperty(environment, "spring.jackson.deserialization.fail-on-null-for-primitives", "true");
	}
}
