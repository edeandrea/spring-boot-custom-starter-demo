package com.mycompany.myframework.autoconfigure.tracing;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.core.env.Environment;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;

import com.mycompany.myframework.tracing.ActiveProfilesSpanAdjuster;
import com.mycompany.myframework.tracing.configuration.TracingProperties;
import com.mycompany.myframework.tracing.reactive.TracingResponseBodyResultHandler;
import com.mycompany.myframework.tracing.servlet.TracingResponseBodyAdvice;

@Configuration
@EnableConfigurationProperties({ TracingProperties.class })
public class TracingAutoConfiguration {
	@Bean
	public ActiveProfilesSpanAdjuster activeProfilesSpanAdjuster(Environment environment) {
		return new ActiveProfilesSpanAdjuster(environment);
	}

	@Configuration
	@ConditionalOnWebApplication(type = Type.SERVLET)
	static class ServletConfiguration {
		@Bean
		@ConditionalOnProperty(prefix = TracingProperties.PREFIX, name = "enable-response-headers")
		public TracingResponseBodyAdvice tracingResponseBodyAdvice() {
			return new TracingResponseBodyAdvice();
		}
	}

	@Configuration
	@ConditionalOnWebApplication(type = Type.REACTIVE)
	static class ReactiveConfiguration {
		@Bean
		@ConditionalOnProperty(prefix = TracingProperties.PREFIX, name = "enable-response-headers")
		public TracingResponseBodyResultHandler tracingResponseBodyResultHandler(ServerCodecConfigurer serverCodecConfigurer,
			RequestedContentTypeResolver webFluxContentTypeResolver, ReactiveAdapterRegistry webFluxAdapterRegistry) {
			return new TracingResponseBodyResultHandler(serverCodecConfigurer.getWriters(), webFluxContentTypeResolver, webFluxAdapterRegistry);
		}
	}
}
