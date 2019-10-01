package com.mycompany.myframework.tracing.servlet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.sleuth.autoconfig.TraceAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import brave.Tracer;
import com.mycompany.myframework.tracing.TracingResponseModifierUtils;
import com.mycompany.myframework.tracing.servlet.TracingResponseBodyAdviceTests.TestConfig;

@SpringBootTest(classes = TestConfig.class)
@AutoConfigureMockMvc(printOnlyOnFailure = false)
public class TracingResponseBodyAdviceTests {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	ApplicationContext applicationContext;

	@Test
	public void contextLoads() {
		assertThat(this.applicationContext).isNotNull();
		assertThat(this.applicationContext.getBeansOfType(TraceAutoConfiguration.class)).isNotNull();
		assertThat(this.applicationContext.getBeansOfType(Tracer.class)).isNotNull();
		assertThat(this.mockMvc).isNotNull();
	}

	@Test
	public void tracingHeadersPresent() throws Exception {
		this.mockMvc.perform(get("/test-api"))
			.andExpect(status().isOk())
			.andExpect(header().exists(TracingResponseModifierUtils.TRACE_HEADER_TRACE_ID))
			.andExpect(header().exists(TracingResponseModifierUtils.TRACE_HEADER_SPAN_EXPORTABLE));
	}

	@Configuration
	@EnableAutoConfiguration
	static class TestConfig {
		@Bean
		public TracingResponseBodyAdvice tracingResponseBodyAdvice() {
			return new TracingResponseBodyAdvice();
		}

		@RestController
		@RequestMapping("/test-api")
		public static class TestApi {
			private static final Logger LOGGER = LoggerFactory.getLogger(TestConfig.TestApi.class);

			@GetMapping
			public String testGet() {
				LOGGER.info("testing sleuth");
				return "GET /test-api";
			}
		}
	}
}
