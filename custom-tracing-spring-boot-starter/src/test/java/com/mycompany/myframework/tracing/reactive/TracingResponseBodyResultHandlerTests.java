package com.mycompany.myframework.tracing.reactive;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebFlux;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.sleuth.autoconfig.TraceAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import org.springframework.web.reactive.config.EnableWebFlux;

import brave.Tracer;
import com.mycompany.myframework.tracing.TracingResponseModifierUtils;
import com.mycompany.myframework.tracing.reactive.TracingResponseBodyResultHandlerTests.TestConfig;
import reactor.core.publisher.Mono;

@SpringBootTest(classes = TestConfig.class)
@AutoConfigureWebTestClient
@AutoConfigureWebFlux
@TestPropertySource(properties = "spring.main.web-application-type=reactive")
public class TracingResponseBodyResultHandlerTests {
	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	public void contextLoads() {
		assertThat(this.applicationContext).isNotNull();
		assertThat(this.applicationContext.getBeansOfType(TraceAutoConfiguration.class)).isNotNull();
		assertThat(this.applicationContext.getBeansOfType(Tracer.class)).isNotNull();
		assertThat(this.applicationContext.getBeansOfType(TestConfig.TestApi.class)).isNotNull();
		assertThat(this.webTestClient).isNotNull();
	}

	@Test
	public void tracingHeadersPresent() throws Exception {
		this.webTestClient.get()
			.uri("/test-api")
			.exchange()
			.expectStatus()
				.is2xxSuccessful()
			.expectHeader()
				.exists(TracingResponseModifierUtils.TRACE_HEADER_TRACE_ID)
			.expectHeader()
				.exists(TracingResponseModifierUtils.TRACE_HEADER_SPAN_EXPORTABLE);
	}

	@Configuration
	@EnableAutoConfiguration
	@EnableWebFlux
	static class TestConfig {
		@Bean
		public TracingResponseBodyResultHandler tracingResponseBodyResultHandler(ServerCodecConfigurer serverCodecConfigurer,
			RequestedContentTypeResolver webFluxContentTypeResolver, ReactiveAdapterRegistry webFluxAdapterRegistry) {
			return new TracingResponseBodyResultHandler(serverCodecConfigurer.getWriters(), webFluxContentTypeResolver, webFluxAdapterRegistry);
		}

		@RestController
		@RequestMapping("/test-api")
		public static class TestApi {
			private static final Logger LOGGER = LoggerFactory.getLogger(TracingResponseBodyResultHandlerTests.TestConfig.TestApi.class);

			@GetMapping
			public Mono<String> testGet() {
				LOGGER.info("testing sleuth reactive");
				return Mono.just("GET /test-api");
			}
		}
	}
}
