package com.mycompany.myframework.autoconfigure.tracing;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;

import brave.p6spy.TracingP6Factory;
import com.p6spy.engine.logging.P6LogFactory;
import com.p6spy.engine.spy.P6SpyFactory;

/**
 * Spring {@link ApplicationListener} for enabling db tracing through <a href="https://github.com/p6spy/p6spy">p6spy</a>.
 * <p>
 *   This {@link ApplicationListener} listens to the {@link ApplicationEnvironmentPreparedEvent}, allowing the system properties to be
 *   set before p6spy is initialized.
 * </p>
 * <p>
 *   As typical with auto-configuration, this backs off if it detects a {@link #P6SPY_MODULE_LIST} property already set.
 * </p>
 */
public class TracingConfigurerApplicationListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {
	static final String P6SPY_MODULE_LIST = "p6spy.config.modulelist";

	private static final String P6SPY_MODULE_LIST_CLASSES =
		Arrays.asList(P6SpyFactory.class, P6LogFactory.class,TracingP6Factory.class)
			.stream()
			.map(Class::getName)
			.collect(Collectors.joining(","));

	@Override
	public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
		Map<String, Object> systemProperties = event.getEnvironment().getSystemProperties();

		if (!systemProperties.containsKey(P6SPY_MODULE_LIST)) {
			systemProperties.put(P6SPY_MODULE_LIST, P6SPY_MODULE_LIST_CLASSES);
		}
	}
}
