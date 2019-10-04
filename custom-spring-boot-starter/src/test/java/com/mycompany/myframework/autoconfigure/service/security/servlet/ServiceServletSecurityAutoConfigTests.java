package com.mycompany.myframework.autoconfigure.service.security.servlet;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.servlet.Filter;

import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.http.codec.CodecsAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.client.WebClientRestTemplateAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.assertj.ApplicationContextAssert;
import org.springframework.boot.test.context.assertj.ApplicationContextAssertProvider;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.DenyAllPermissionEvaluator;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.header.writers.frameoptions.AllowFromStrategy;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.web.filter.CorsFilter;

import com.mycompany.myframework.autoconfigure.properties.PropertiesAutoConfiguration;
import com.mycompany.myframework.autoconfigure.service.security.servlet.ServiceServletSecurityAutoConfig.MethodSecurityAutoConfig;
import com.mycompany.myframework.autoconfigure.service.security.servlet.ServiceServletSecurityAutoConfig.SwaggerAutoConfiguration;
import com.mycompany.myframework.autoconfigure.service.swagger.servlet.SwaggerServletSecurityResponseModifier;
import com.mycompany.myframework.properties.config.MyFrameworkConfig;
import com.mycompany.myframework.service.security.servlet.AllowedDomainsCorsConfigurationSource;
import com.mycompany.myframework.service.security.servlet.HeaderUserFilter;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

public class ServiceServletSecurityAutoConfigTests {
	private static final String SECURITY_ENABLED_STRING = String.format("%s.security.enabled=true", MyFrameworkConfig.PREFIX);
	private static final String CORS_ALLOWED_DOMAINS_STRING = String.format("%s.security.cors.allowed-domains=redhat.com,redhat.net", MyFrameworkConfig.PREFIX);

	private static final AutoConfigurations AUTO_CONFIGURATIONS =
		AutoConfigurations.of(
			PropertiesAutoConfiguration.class,
			ServiceServletSecurityAutoConfig.class,
			DispatcherServletAutoConfiguration.class,
			ServletWebServerFactoryAutoConfiguration.class,
			WebMvcAutoConfiguration.class,
			SecurityAutoConfiguration.class,
			SecurityFilterAutoConfiguration.class,
			HttpMessageConvertersAutoConfiguration.class,
			CodecsAutoConfiguration.class,
			JacksonAutoConfiguration.class,
			WebClientRestTemplateAutoConfiguration.class,
			RestTemplateAutoConfiguration.class
		);

	@Test
	public void configNotPresentBecauseNotWebApplication() {
		new ApplicationContextRunner()
			.withConfiguration(AUTO_CONFIGURATIONS)
			.run(this::assertNoConfigPresent);
	}

	@Test
	public void configNotPresentBecauseIsReactiveApplication() {
		new ReactiveWebApplicationContextRunner()
			.withConfiguration(AUTO_CONFIGURATIONS)
			.run(this::assertNoConfigPresent);
	}

	@Test
	public void configNotPresentBecauseEnabledPropertyNotSet() {
		new WebApplicationContextRunner()
			.withConfiguration(AUTO_CONFIGURATIONS)
			.run(this::assertNoConfigPresent);
	}

	@Test
	public void configNotPresentBecauseSpringSecurityClassesNotPresent() {
		new WebApplicationContextRunner()
			.withClassLoader(new FilteredClassLoader("org.springframework.security"))
			.withConfiguration(AUTO_CONFIGURATIONS)
			.withPropertyValues(SECURITY_ENABLED_STRING)
			.run(this::assertNoConfigPresent);
	}

	@Test
	public void noAllowedDomainsCorsConfigurationSource() {
		new WebApplicationContextRunner()
			.withConfiguration(AUTO_CONFIGURATIONS)
			.withPropertyValues(SECURITY_ENABLED_STRING)
			.run(context -> {
				assertThat(context)
					.doesNotHaveBean(AllowedDomainsCorsConfigurationSource.class);
			});
	}

	@Test
	public void everythingPresent() {
		new WebApplicationContextRunner()
			.withConfiguration(AUTO_CONFIGURATIONS)
			.withUserConfiguration(PermissionEvaluatorConfig.class, SwaggerConfig.class, UserDetailsServiceConfig.class)
			.withPropertyValues(SECURITY_ENABLED_STRING, CORS_ALLOWED_DOMAINS_STRING)
			.run(context -> {
				assertConfigClasses(
					context,
					Arrays.asList(
						MyFrameworkConfig.class,
						PropertiesAutoConfiguration.class,
						ServiceServletSecurityAutoConfig.class,
						CsrfTokenRepository.class,
						MethodSecurityAutoConfig.class,
						PermissionEvaluator.class,
						SwaggerAutoConfiguration.class,
						SwaggerServletSecurityResponseModifier.class,
						AllowedDomainsCorsConfigurationSource.class
					),
					Collections.EMPTY_LIST
				);

				List<SecurityFilterChain> filterChains = context.getBean("springSecurityFilterChain", FilterChainProxy.class).getFilterChains();

				assertThat(filterChains)
					.isNotNull()
					.isNotEmpty()
					.hasSize(1);

				SecurityFilterChain securityFilterChain = filterChains.get(0);

				assertThat(securityFilterChain)
					.isNotNull()
					.isInstanceOf(DefaultSecurityFilterChain.class)
					.extracting(fc -> ((DefaultSecurityFilterChain)fc).getRequestMatcher())
					.isEqualTo(AnyRequestMatcher.INSTANCE);

				List<Filter> filters = securityFilterChain.getFilters();

				assertThat(filters)
					.isNotNull()
					.isNotEmpty();

				assertThat(filters.stream().filter(HeaderUserFilter.class::isInstance).findAny())
					.isNotNull()
					.isNotEmpty();

				assertThat(filters.stream().filter(CorsFilter.class::isInstance).findAny())
					.isNotNull()
					.isNotEmpty();
			});
	}

	private <C extends ConfigurableApplicationContext, A extends ApplicationContextAssertProvider<C>> void assertNoConfigPresent(A context) {
		assertConfigClasses(
			context,
			Arrays.asList(
				MyFrameworkConfig.class,
				PropertiesAutoConfiguration.class
			),
			Arrays.asList(
				ServiceServletSecurityAutoConfig.class,
				UserDetailsService.class,
				CsrfTokenRepository.class,
				AllowFromStrategy.class,
				SwaggerAutoConfiguration.class,
				PermissionEvaluator.class,
				SwaggerServletSecurityResponseModifier.class,
				AllowedDomainsCorsConfigurationSource.class
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
		public UserDetailsService userDetailsService() {
			return username -> User.withUsername(username).build();
		}
	}

	@Configuration
	@EnableSwagger2
	static class SwaggerConfig {

	}

	@Configuration
	static class PermissionEvaluatorConfig {
		@Bean
		public PermissionEvaluator permissionEvaluator() {
			return new DenyAllPermissionEvaluator();
		}
	}
}
