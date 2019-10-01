package com.mycompany.myframework.autoconfigure.tracing;

import java.util.LinkedHashMap;
import java.util.Optional;

import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.boot.origin.OriginTrackedValue;
import org.springframework.boot.origin.PropertySourceOrigin;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;

/**
 * Spring {@link ApplicationContextInitializer} for setting the 128 bit trace ids by default
 */
public class TracingApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {
	int DEFAULT_ORDER = Ordered.LOWEST_PRECEDENCE - 50;
	private String DEFAULT_PROPERTIES_KEY = "myframeworkDefaultProperties";
	static final String TRACE_ID_128_PROPERTY_NAME = "spring.sleuth.trace-id128";
	static final String SLEUTH_SAMPLER_PROBABILITY_PROPERTY = "spring.sleuth.sampler.probability";

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		ConfigurableEnvironment environment = applicationContext.getEnvironment();
		boolean springSleuthEnabled = environment.getProperty("spring.sleuth.enabled", Boolean.class, true);

		if (springSleuthEnabled) {
			boolean containsTraceId128Property = environment.containsProperty(TRACE_ID_128_PROPERTY_NAME);
			boolean containerSamplerProbabilityProperty = environment.containsProperty(SLEUTH_SAMPLER_PROBABILITY_PROPERTY);

			if (!containsTraceId128Property) {
				addDefaultProperty(environment, TRACE_ID_128_PROPERTY_NAME, String.valueOf(true));
			}

			if (!containerSamplerProbabilityProperty) {
				addDefaultProperty(environment, SLEUTH_SAMPLER_PROBABILITY_PROPERTY, "0.5");
			}
		}
	}

	private void addDefaultProperty(ConfigurableEnvironment environment, String name, String value) {
		MutablePropertySources sources = environment.getPropertySources();
		OriginTrackedMapPropertySource defaultPropertiesSource;

		if (sources.contains(this.DEFAULT_PROPERTIES_KEY)) {
			defaultPropertiesSource = Optional.ofNullable(sources.get(this.DEFAULT_PROPERTIES_KEY))
				.filter(OriginTrackedMapPropertySource.class::isInstance)
				.map(OriginTrackedMapPropertySource.class::cast)
				.orElseThrow(() -> new IllegalArgumentException(String.format("Property defaultPropertiesSource %s is not of type %s", this.DEFAULT_PROPERTIES_KEY, OriginTrackedMapPropertySource.class.getName())));
		}
		else {
			defaultPropertiesSource = new OriginTrackedMapPropertySource(this.DEFAULT_PROPERTIES_KEY, new LinkedHashMap<>());
			sources.addFirst(defaultPropertiesSource);
		}

		defaultPropertiesSource.getSource().put(name, OriginTrackedValue.of(value, PropertySourceOrigin.get(defaultPropertiesSource, name)));
	}

	@Override
	public int getOrder() {
		return this.DEFAULT_ORDER;
	}
}
