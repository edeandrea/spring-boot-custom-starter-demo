package com.mycompany.myframework.autoconfigure.service.security.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsByNameServiceWrapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;

import com.mycompany.myframework.autoconfigure.service.security.ConditionalOnNoJwtTokenParsing;
import com.mycompany.myframework.autoconfigure.service.swagger.servlet.SwaggerServletSecurityResponseModifier;
import com.mycompany.myframework.service.security.servlet.HeaderUserDetailsService;
import com.mycompany.myframework.service.security.servlet.HeaderUserFilter;
import springfox.documentation.swagger.web.ApiKeyVehicle;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@ConditionalOnServletSecurityEnabled
public class ServiceServletSecurityAutoConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceServletSecurityAutoConfig.class);

	@Bean
	@ConditionalOnMissingBean
	public CsrfTokenRepository csrfTokenRepository() {
		return CookieCsrfTokenRepository.withHttpOnlyFalse();
	}

	@Bean
	@ConditionalOnMissingBean
	public UserDetailsService userDetailsService() {
		return new HeaderUserDetailsService();
	}

	@Configuration
	@ConditionalOnNoJwtTokenParsing
	@EnableWebSecurity
	@Order(100)
	static class NoJwtTokenWebSecurityConfiguration extends WebSecurityConfigurerAdapter {
		@Autowired
		private ObjectProvider<WebEndpointProperties> webEndpointProperties;

		@Autowired
		private ObjectProvider<ManagementServerProperties> managementServerProperties;

		@Autowired
		private UserDetailsService userDetailsService;

		@Value("${springfox.documentation.swagger.v2.path:/v2/api-docs}")
		private String swaggerApiPath;

		@Value("${reactive.servlet.context-path:/}")
		private String serverContextPath;

		@Autowired
		private CsrfTokenRepository csrfTokenRepository;

		@Override
		protected void configure(AuthenticationManagerBuilder auth) throws Exception {
			ServiceServletSecurityAutoConfig.LOGGER.info("Enabling authentication provider");
			PreAuthenticatedAuthenticationProvider provider = new PreAuthenticatedAuthenticationProvider();
			provider.setPreAuthenticatedUserDetailsService(new UserDetailsByNameServiceWrapper<>(this.userDetailsService));
			auth.authenticationProvider(provider);
		}

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http
				.cors().and()
				.headers().and()
				.csrf().csrfTokenRepository(this.csrfTokenRepository).and()
				.logout()
					.deleteCookies("XSRF-TOKEN")
					.logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler()).permitAll();

			String[] bypassSecurityUris = {
				prependContextPath(getActuatorRoot()),
				prependContextPath(String.format("%s/**", this.swaggerApiPath)),
				prependContextPath("/swagger-ui.html"),
				prependContextPath("/swagger-resources/**"),
				prependContextPath("/webjars/springfox-swagger-ui/**")
			};

			http
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
				.authorizeRequests()
					.requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll()
					.antMatchers(bypassSecurityUris).permitAll()
					.anyRequest().authenticated().and()
				.addFilterBefore(new HeaderUserFilter(authenticationManagerBean(), "SM_USER"), AbstractPreAuthenticatedProcessingFilter.class)
				.exceptionHandling()
					.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
					.accessDeniedHandler(new AccessDeniedHandlerImpl());
		}

		private String getActuatorRoot() {
			String actuatorRoot = String.format("%s%s",
				this.managementServerProperties.getIfAvailable(ManagementServerProperties::new).getServlet().getContextPath(),
				this.webEndpointProperties.getIfAvailable(WebEndpointProperties::new).getBasePath());

			ServiceServletSecurityAutoConfig.LOGGER.info("Computed actuatorRoot = {}", actuatorRoot);

			return actuatorRoot;
		}

		private String prependContextPath(String path) {
			return StringUtils.equals(this.serverContextPath, "/") ? path : String.format("%s%s", this.serverContextPath, path);
		}
	}

	@Configuration
	@EnableGlobalMethodSecurity(prePostEnabled = true)
	@ConditionalOnBean({ PermissionEvaluator.class })
	static class MethodSecurityAutoConfig {

	}

	@Configuration
	@ConditionalOnBean(annotation = EnableSwagger2.class)
	@ComponentScan(basePackageClasses = { SwaggerServletSecurityResponseModifier.class })
	static class SwaggerAutoConfiguration {
		@Bean
		public SecurityConfiguration springfoxSecurityConfig() {
			return new SecurityConfiguration(
				"app-client-id",
				"app-client-secret",
				"app-realm",
				"app",
				"{{X-XSRF-TOKEN}}",
				ApiKeyVehicle.HEADER,
				"X-XSRF-TOKEN",
				","
			);
		}
	}
}
