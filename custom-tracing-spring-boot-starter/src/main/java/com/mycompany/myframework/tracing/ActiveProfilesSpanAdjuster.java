package com.mycompany.myframework.tracing;

import java.util.Arrays;

import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import brave.handler.FinishedSpanHandler;
import brave.handler.MutableSpan;
import brave.propagation.TraceContext;

/**
 * Adds appropriate tags to a tracing span to give context for search and analysis
 */
@Component
public class ActiveProfilesSpanAdjuster extends FinishedSpanHandler {
	private final Environment environment;

	public ActiveProfilesSpanAdjuster(Environment environment) {
		this.environment = environment;
	}

	@Override
	public boolean handle(TraceContext context, MutableSpan span) {
		span.tag(ConfigFileApplicationListener.ACTIVE_PROFILES_PROPERTY, Arrays.toString(this.environment.getActiveProfiles()));
		return true;
	}
}
