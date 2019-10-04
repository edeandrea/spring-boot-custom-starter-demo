package com.mycompany.myframework.properties.config;

import javax.validation.Valid;

import org.apache.commons.lang3.builder.ToStringBuilder;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = MyFrameworkConfig.PREFIX)
@Validated
public class MyFrameworkConfig {
	public static final String PREFIX = "mycompany.myframework.config";

	@Valid
	@NestedConfigurationProperty
	private final SecurityConfig security = new SecurityConfig();

	private boolean enableActuators = false;

	public SecurityConfig getSecurity() {
		return this.security;
	}

	public boolean isEnableActuators() {
		return this.enableActuators;
	}

	public void setEnableActuators(boolean enableActuators) {
		this.enableActuators = enableActuators;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public static class SecurityConfig {
		private boolean enabled = false;

		/**
		 * CORS sub-configuration
		 */
		@NestedConfigurationProperty
		@Valid
		private final CorsConfig cors = new CorsConfig();

		public boolean isEnabled() {
			return this.enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		/**
		 * Gets the CORS sub-configuration
		 * @return The CORS sub-configuration
		 */
		public CorsConfig getCors() {
			return this.cors;
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}

		public static class CorsConfig {
			/**
			 * A comma-separated list of allowed domains. Can be full root domains or subdomains (i.e. aig.com, subdomain.aig.net, etc)
			 */
			@Nullable
			private String allowedDomains;

			/**
			 * Gets the comma-separated list of allowed domains
			 * @return The comma-separated list of allowed domains
			 */
			@Nullable
			public String getAllowedDomains() {
				return this.allowedDomains;
			}

			/**
			 * Sets a comma-separated list of allowed domains. Can be full root domains or subdomains (i.e. aig.com, subdomain.aig.net, etc)
			 * @param allowedDomains The comma-separated list of allowed domains
			 */
			public void setAllowedDomains(@Nullable String allowedDomains) {
				this.allowedDomains = allowedDomains;
			}

			@Override
			public String toString() {
				return ToStringBuilder.reflectionToString(this);
			}
		}
	}
}
