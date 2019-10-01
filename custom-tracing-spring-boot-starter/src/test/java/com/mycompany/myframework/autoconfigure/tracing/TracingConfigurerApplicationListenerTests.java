package com.mycompany.myframework.autoconfigure.tracing;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.mock.env.MockEnvironment;

public class TracingConfigurerApplicationListenerTests {
	@Test
	public void onApplicationEvent() {
		MockEnvironment mockEnvironment = new MockEnvironment();
		ApplicationEnvironmentPreparedEvent applicationEnvironmentPreparedEvent = new ApplicationEnvironmentPreparedEvent(new SpringApplication(), null, mockEnvironment);
		new TracingConfigurerApplicationListener().onApplicationEvent(applicationEnvironmentPreparedEvent);

		assertThat(mockEnvironment.getSystemProperties().containsKey(TracingConfigurerApplicationListener.P6SPY_MODULE_LIST))
			.isTrue();
		assertThat(mockEnvironment.getSystemProperties().get(TracingConfigurerApplicationListener.P6SPY_MODULE_LIST))
			.isEqualTo("com.p6spy.engine.spy.P6SpyFactory,com.p6spy.engine.logging.P6LogFactory,brave.p6spy.TracingP6Factory");
	}

	@Test
	public void onApplicationEventBackoff() {
		String applicationOverrideModuleList = "applicationOverrideModuleList";
		MockEnvironment mockEnvironment = new MockEnvironment();
		mockEnvironment.getSystemProperties().put(TracingConfigurerApplicationListener.P6SPY_MODULE_LIST, applicationOverrideModuleList);

		ApplicationEnvironmentPreparedEvent applicationEnvironmentPreparedEvent = new ApplicationEnvironmentPreparedEvent(new SpringApplication(), null, mockEnvironment);
		new TracingConfigurerApplicationListener().onApplicationEvent(applicationEnvironmentPreparedEvent);
		assertThat(mockEnvironment.getSystemProperties().containsKey(TracingConfigurerApplicationListener.P6SPY_MODULE_LIST))
			.isTrue();
		assertThat(mockEnvironment.getSystemProperties().get(TracingConfigurerApplicationListener.P6SPY_MODULE_LIST))
			.isEqualTo(applicationOverrideModuleList);
	}
}
