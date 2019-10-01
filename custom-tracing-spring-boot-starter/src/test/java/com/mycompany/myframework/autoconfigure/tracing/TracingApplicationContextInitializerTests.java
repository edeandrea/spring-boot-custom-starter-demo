package com.mycompany.myframework.autoconfigure.tracing;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.mock.env.MockEnvironment;

public class TracingApplicationContextInitializerTests {
	@Test
	public void defaultPropertiesSet() {
		MockEnvironment mockEnvironment = getSleuthEnvironment(true);
		new TracingApplicationContextInitializer().initialize(createContext(mockEnvironment));

		assertThat(mockEnvironment.getProperty(TracingApplicationContextInitializer.TRACE_ID_128_PROPERTY_NAME, Boolean.class))
			.isTrue();

		assertThat(mockEnvironment.getProperty(TracingApplicationContextInitializer.SLEUTH_SAMPLER_PROBABILITY_PROPERTY, Double.class))
			.isEqualTo(0.5);
	}

	@Test
	public void defaultTraceId128PropertyDoesNotOverride() {
		MockEnvironment mockEnvironment =  getSleuthEnvironment(true)
			.withProperty(TracingApplicationContextInitializer.TRACE_ID_128_PROPERTY_NAME, "false");
		new TracingApplicationContextInitializer().initialize(createContext(mockEnvironment));

		assertThat(mockEnvironment.getProperty(TracingApplicationContextInitializer.TRACE_ID_128_PROPERTY_NAME, Boolean.class))
			.isFalse();
	}

	@Test
	public void defaultTraceId128PropertyAbsent() {
		MockEnvironment mockEnvironment = getSleuthEnvironment(false);
		new TracingApplicationContextInitializer().initialize(createContext(mockEnvironment));

		assertThat(mockEnvironment.getProperty(TracingApplicationContextInitializer.TRACE_ID_128_PROPERTY_NAME, Boolean.class))
			.isNull();
	}

	@Test
	public void defaultSamplerPropertyDoesNotOverride() {
		MockEnvironment mockEnvironment =  getSleuthEnvironment(true)
			.withProperty(TracingApplicationContextInitializer.SLEUTH_SAMPLER_PROBABILITY_PROPERTY, "1.0");
		new TracingApplicationContextInitializer().initialize(createContext(mockEnvironment));

		assertThat(mockEnvironment.getProperty(TracingApplicationContextInitializer.SLEUTH_SAMPLER_PROBABILITY_PROPERTY, Double.class))
			.isEqualTo(1.0);
	}

	@Test
	public void defaultSamplerPropertyAbsent() {
		MockEnvironment mockEnvironment = getSleuthEnvironment(false);
		new TracingApplicationContextInitializer().initialize(createContext(mockEnvironment));

		assertThat(mockEnvironment.getProperty(TracingApplicationContextInitializer.SLEUTH_SAMPLER_PROBABILITY_PROPERTY, Double.class))
			.isNull();
	}

	private MockEnvironment getSleuthEnvironment(boolean enabled) {
		return new MockEnvironment()
			.withProperty("spring.sleuth.enabled", Boolean.toString(enabled));
	}

	private static ConfigurableApplicationContext createContext(ConfigurableEnvironment environment) {
		ConfigurableApplicationContext context = new StaticApplicationContext();
		context.setEnvironment(environment);

		return context;
	}
}
