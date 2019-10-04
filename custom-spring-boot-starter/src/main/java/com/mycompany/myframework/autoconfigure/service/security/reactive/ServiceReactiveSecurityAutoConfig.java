package com.mycompany.myframework.autoconfigure.service.security.reactive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxRegistrations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authentication.ServerAuthenticationEntryPointFailureHandler;
import org.springframework.security.web.server.authentication.logout.HttpStatusReturningServerLogoutSuccessHandler;
import org.springframework.security.web.server.authorization.HttpStatusServerAccessDeniedHandler;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.ServerCsrfTokenRepository;
import org.springframework.web.cors.reactive.CorsProcessor;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;

import com.mycompany.myframework.autoconfigure.service.security.ConditionalOnNoJwtTokenParsing;
import com.mycompany.myframework.properties.config.MyFrameworkConfig;
import com.mycompany.myframework.service.security.server.HeaderReactiveUserDetailsService;
import com.mycompany.myframework.service.security.server.RequestHeaderServerAuthenticationConverter;
import com.mycompany.myframework.service.security.server.ServerAllowedDomainsCorsConfigurationSource;
import com.mycompany.myframework.service.security.server.ServerAllowedDomainsCorsProcessor;
import com.mycompany.myframework.service.security.server.ServerCsrfTokenSubscribingResponseModifier;
import com.mycompany.myframework.service.security.server.UserDetailsRepositoryReactiveAuthenticationManager;

@Configuration
@ConditionalOnReactiveSecurityEnabled
public class ServiceReactiveSecurityAutoConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceReactiveSecurityAutoConfig.class);

	@Bean
	@ConditionalOnMissingBean
	public ServerCsrfTokenRepository csrfTokenRepository() {
		return CookieServerCsrfTokenRepository.withHttpOnlyFalse();
	}

	@Bean
	@ConditionalOnBean(ServerCsrfTokenRepository.class)
	public ServerCsrfTokenSubscribingResponseModifier serverCsrfTokenSubscribingResponseModifier(ServerCodecConfigurer serverCodecConfigurer, RequestedContentTypeResolver webFluxContentTypeResolver, ReactiveAdapterRegistry webFluxAdapterRegistry) {
		return new ServerCsrfTokenSubscribingResponseModifier(serverCodecConfigurer.getWriters(), webFluxContentTypeResolver, webFluxAdapterRegistry);
	}

	@Bean
	@ConditionalOnMissingBean
	public ReactiveUserDetailsService userDetailsService() {
		return new HeaderReactiveUserDetailsService();
	}

	@Configuration
	@ConditionalOnProperty(prefix = MyFrameworkConfig.PREFIX + ".security.cors", name = "allowed-domains")
	@AutoConfigureBefore(WebFluxAutoConfiguration.class)
	static class AllowedDomainsCorsConfiguration implements WebFluxRegistrations {
		@Autowired
		private CorsProcessor corsProcessor;

		@Bean
		public CorsProcessor allowedDomainsCorsProcessor(MyFrameworkConfig frameworkConfig) {
			ServiceReactiveSecurityAutoConfig.LOGGER.info("Injecting {} because {}.security.cors.allowed-domains is present", ServerAllowedDomainsCorsProcessor.class.getName(), MyFrameworkConfig.PREFIX);
			return new ServerAllowedDomainsCorsProcessor(new ServerAllowedDomainsCorsConfigurationSource(frameworkConfig.getSecurity().getCors()));
		}

		@Override
		public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
			RequestMappingHandlerMapping requestMappingHandlerMapping = new RequestMappingHandlerMapping();
			requestMappingHandlerMapping.setCorsProcessor(this.corsProcessor);

			return requestMappingHandlerMapping;
		}
	}

	@Configuration
	@ConditionalOnNoJwtTokenParsing
	@EnableWebFluxSecurity
	@Order(100)
	static class NoJwtTokenWebSecurityConfiguration {
		@Bean
		public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, ServerCsrfTokenRepository csrfTokenRepository, ReactiveUserDetailsService userDetailsService) {
			UserDetailsRepositoryReactiveAuthenticationManager authenticationManager = new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
			authenticationManager.setPostAuthenticationChecks(new AccountStatusUserDetailsChecker());
			authenticationManager.setPasswordEncoder(NoOpPasswordEncoder.getInstance()); // There's no password here in header-based authentication
			HttpStatusServerEntryPoint authenticationEntryPoint = new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED);
			AuthenticationWebFilter authenticationWebFilter = new AuthenticationWebFilter(authenticationManager);
			authenticationWebFilter.setAuthenticationFailureHandler(new ServerAuthenticationEntryPointFailureHandler(authenticationEntryPoint));
			authenticationWebFilter.setServerAuthenticationConverter(new RequestHeaderServerAuthenticationConverter());

			return http
				.cors().and()
				.headers().and()
				.logout().logoutSuccessHandler(new HttpStatusReturningServerLogoutSuccessHandler()).and()
				.csrf().disable()
				.authorizeExchange()
					.matchers(EndpointRequest.toAnyEndpoint()).permitAll()
					.anyExchange().authenticated().and()
				.authenticationManager(authenticationManager)
				.addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
				.exceptionHandling()
					.authenticationEntryPoint(authenticationEntryPoint)
					.accessDeniedHandler(new HttpStatusServerAccessDeniedHandler(HttpStatus.FORBIDDEN)).and()
				.build();
		}
	}

	@Configuration
	@EnableReactiveMethodSecurity
	static class MethodSecurityAutoConfiguration {

	}
}
