package com.mycompany.myframework.autoconfigure.service.security.reactive;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.http.codec.CodecsAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.assertj.ApplicationContextAssert;
import org.springframework.boot.test.context.assertj.ApplicationContextAssertProvider;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.server.MatcherSecurityWebFilterChain;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.csrf.CsrfWebFilter;
import org.springframework.security.web.server.csrf.ServerCsrfTokenRepository;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.reactive.CorsProcessor;
import org.springframework.web.server.WebFilter;

import com.mycompany.myframework.autoconfigure.properties.PropertiesAutoConfiguration;
import com.mycompany.myframework.autoconfigure.service.security.reactive.ServiceReactiveSecurityAutoConfig.AllowedDomainsCorsConfiguration;
import com.mycompany.myframework.autoconfigure.service.security.reactive.ServiceReactiveSecurityAutoConfig.MethodSecurityAutoConfiguration;
import com.mycompany.myframework.properties.config.MyFrameworkConfig;
import com.mycompany.myframework.service.security.server.ServerAllowedDomainsCorsProcessor;
import reactor.core.publisher.Mono;

public class ServiceReactiveSecurityAutoConfigTests {
	private static final String SECURITY_ENABLED_STRING = String.format("%s.security.enabled=true", MyFrameworkConfig.PREFIX);
	private static final String CORS_ALLOWED_DOMAINS_STRING = String.format("%s.security.cors.allowed-domains=redhat.com,redhat.net", MyFrameworkConfig.PREFIX);

	private static final AutoConfigurations AUTO_CONFIGURATIONS =
		AutoConfigurations.of(
			PropertiesAutoConfiguration.class,
			ServiceReactiveSecurityAutoConfig.class,
			WebFluxAutoConfiguration.class,
			ReactiveSecurityAutoConfiguration.class,
			ReactiveUserDetailsServiceAutoConfiguration.class,
			HttpMessageConvertersAutoConfiguration.class,
			CodecsAutoConfiguration.class,
			JacksonAutoConfiguration.class,
			WebClientAutoConfiguration.class
		);

	@Test
	public void noAllowedDomainsCorsConfigurationSource() {
		new ReactiveWebApplicationContextRunner()
			.withConfiguration(AUTO_CONFIGURATIONS)
			.withPropertyValues(SECURITY_ENABLED_STRING)
			.run(context -> {
				assertThat(context)
					.doesNotHaveBean(AllowedDomainsCorsConfiguration.class)
					.doesNotHaveBean(ServerAllowedDomainsCorsProcessor.class);
			});
	}

	@Test
	public void configNotPresentBecauseNotWebApplication() {
		new ApplicationContextRunner()
			.withConfiguration(AUTO_CONFIGURATIONS)
			.run(this::assertNoConfigPresent);
	}

	@Test
	public void configNotPresentBecauseIsNotReactiveApplication() {
		new WebApplicationContextRunner()
			.withConfiguration(AUTO_CONFIGURATIONS)
			.run(this::assertNoConfigPresent);
	}

	@Test
	public void configNotPresentBecauseEnabledPropertyNotSet() {
		new ReactiveWebApplicationContextRunner()
			.withConfiguration(AUTO_CONFIGURATIONS)
			.run(this::assertNoConfigPresent);
	}

	@Test
	public void configNotPresentBecauseSpringSecurityClassesNotPresent() {
		new ReactiveWebApplicationContextRunner()
			.withClassLoader(new FilteredClassLoader("org.springframework.security"))
			.withConfiguration(AUTO_CONFIGURATIONS)
			.withPropertyValues(SECURITY_ENABLED_STRING)
			.run(this::assertNoConfigPresent);
	}

	@Test
	public void everythingPresent() {
		new ReactiveWebApplicationContextRunner()
			.withConfiguration(AUTO_CONFIGURATIONS)
			.withUserConfiguration(UserDetailsServiceConfig.class)
			.withPropertyValues(
				SECURITY_ENABLED_STRING,
				CORS_ALLOWED_DOMAINS_STRING
			)
			.run(context -> {
				assertConfigClasses(
					context,
					Arrays.asList(
						MyFrameworkConfig.class,
						PropertiesAutoConfiguration.class,
						ServiceReactiveSecurityAutoConfig.class,
						MethodSecurityAutoConfiguration.class,
						AllowedDomainsCorsConfiguration.class,
						ServerAllowedDomainsCorsProcessor.class,
						ReactiveUserDetailsService.class,
						ServerCsrfTokenRepository.class,
						CorsProcessor.class,
						ServerAllowedDomainsCorsProcessor.class
					),
					Collections.EMPTY_LIST
				);

				SecurityWebFilterChain filterChain = context.getBean(SecurityWebFilterChain.class);

				assertThat(filterChain)
					.isNotNull()
					.isExactlyInstanceOf(MatcherSecurityWebFilterChain.class);

				List<WebFilter> filters = (List<WebFilter>) ReflectionTestUtils.getField(filterChain, "filters");

				assertThat(filters)
					.isNotNull()
					.isNotEmpty();

				assertThat(filters.stream().filter(CsrfWebFilter.class::isInstance).findAny())
					.isNotNull()
					.isEmpty();

				assertThat(filters.stream().filter(AuthenticationWebFilter.class::isInstance).findAny())
					.isNotNull()
					.isNotEmpty();
			});
	}

	private <C extends ConfigurableApplicationContext, A extends ApplicationContextAssertProvider<C>> void assertNoConfigPresent(A context) {
		assertConfigClasses(
			context,
			Arrays.asList(
				PropertiesAutoConfiguration.class,
				MyFrameworkConfig.class
			),
			Arrays.asList(
				ServiceReactiveSecurityAutoConfig.class,
				ServerCsrfTokenRepository.class,
				MethodSecurityAutoConfiguration.class,
				AllowedDomainsCorsConfiguration.class,
				ServerAllowedDomainsCorsProcessor.class,
				CorsProcessor.class
			));
	}

	private <C extends ConfigurableApplicationContext, A extends ApplicationContextAssertProvider<C>> void assertConfigClasses(A context, Collection<Class<?>> classesShouldBePresent, Collection<Class<?>> classesShouldntBePresent) {
		ApplicationContextAssert<C> contextAssert = assertThat(context);

		classesShouldBePresent.forEach(contextAssert::hasSingleBean);
		classesShouldntBePresent.forEach(contextAssert::doesNotHaveBean);
	}

	@Configuration
	static class UserDetailsServiceConfig {
		@Bean
		public ReactiveUserDetailsService userDetailsService() {
			return username -> Mono.fromSupplier(() -> User.withUsername(username).build());
		}
	}
}
