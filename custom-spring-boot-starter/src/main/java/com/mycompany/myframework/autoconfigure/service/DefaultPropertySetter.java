package com.mycompany.myframework.autoconfigure.service;

import java.util.LinkedHashMap;
import java.util.Optional;

import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.boot.origin.OriginTrackedValue;
import org.springframework.boot.origin.PropertySourceOrigin;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;

public final class DefaultPropertySetter {
	private static final String DEFAULT_PROPERTIES_KEY = "myFrameworkDefaultProperties";

	private DefaultPropertySetter() {
		super();
	}

	public static final void addDefaultProperty(ConfigurableEnvironment environment, String name, String value) {
		MutablePropertySources sources = environment.getPropertySources();
		OriginTrackedMapPropertySource myFrameworkDefaultPropertiesSource;

		if (sources.contains(DEFAULT_PROPERTIES_KEY)) {
			myFrameworkDefaultPropertiesSource = Optional.ofNullable(sources.get(DEFAULT_PROPERTIES_KEY))
				.filter(OriginTrackedMapPropertySource.class::isInstance)
				.map(OriginTrackedMapPropertySource.class::cast)
				.orElseThrow(() -> new IllegalArgumentException(String.format("Property myFrameworkDefaultPropertiesSource %s is not of type %s", DEFAULT_PROPERTIES_KEY, OriginTrackedMapPropertySource.class.getName())));
		}
		else {
			myFrameworkDefaultPropertiesSource = new OriginTrackedMapPropertySource(DEFAULT_PROPERTIES_KEY, new LinkedHashMap<>());
			sources.addFirst(myFrameworkDefaultPropertiesSource);
		}

		myFrameworkDefaultPropertiesSource.getSource().put(name, OriginTrackedValue.of(value, PropertySourceOrigin.get(myFrameworkDefaultPropertiesSource, name)));
	}
}
