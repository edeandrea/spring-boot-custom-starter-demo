package com.mycompany.myframework.tracing.configuration;

import org.apache.commons.lang3.builder.ToStringBuilder;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = TracingProperties.PREFIX)
@Validated
public class TracingProperties {
	public static final String PREFIX = "mycompany.myframework.tracing";

	/**
	 * Should tracing headers be added to the response. Generally only client-facing APIs would need/want to enable.
	 */
	private boolean enableResponseHeaders;

	public boolean isEnableResponseHeaders() {
		return this.enableResponseHeaders;
	}

	public void setEnableResponseHeaders(boolean enableResponseHeaders) {
		this.enableResponseHeaders = enableResponseHeaders;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
