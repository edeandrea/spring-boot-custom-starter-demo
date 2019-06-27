package com.mycompany.myframework.properties.config;

import javax.validation.Valid;

import org.apache.commons.lang3.builder.ToStringBuilder;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
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

		public boolean isEnabled() {
			return this.enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}
	}
}
