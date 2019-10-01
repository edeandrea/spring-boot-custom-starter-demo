package com.mycompany.myframework.tracing;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.sleuth.util.ArrayListSpanReporter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import brave.Tracer;
import brave.sampler.Sampler;
import zipkin2.reporter.Reporter;

@SpringBootTest(
	classes = ActiveProfilesSpanAdjusterTests.TestConfig.class,
	webEnvironment = WebEnvironment.NONE
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(properties = { "spring.zipkin.baseUrl=" })
public class ActiveProfilesSpanAdjusterTests {
	@Autowired
	private ArrayListSpanReporter reporter;

	@Autowired
	private Tracer tracer;

	@Autowired
	private ActiveProfilesSpanAdjuster activeProfilesSpanAdjuster;

	@Test
	void testNoProfilesPresent() {
		reportNewSpan();
		assertThat(this.reporter.getSpans())
			.hasSize(1);
		assertThat(this.reporter.getSpans().get(0).tags())
			.hasSize(1)
			.contains(entry("spring.profiles.active", "[]"));
	}

	@Test
	void testActiveProfilePresent() {
		MockEnvironment mockEnvironment = new MockEnvironment().withProperty("spring.profiles.active", "prod");
		ReflectionTestUtils.setField(this.activeProfilesSpanAdjuster, "environment", mockEnvironment);
		reportNewSpan();
		assertThat(this.reporter.getSpans())
			.hasSize(1);
		assertThat(this.reporter.getSpans().get(0).tags())
			.hasSize(1)
			.contains(entry("spring.profiles.active", "[prod]"));
	}

	@Test
	void testActiveProfilesPresent() {
		MockEnvironment mockEnvironment = new MockEnvironment().withProperty("spring.profiles.active", "prod, cloud");
		ReflectionTestUtils.setField(this.activeProfilesSpanAdjuster, "environment", mockEnvironment);
		reportNewSpan();

		assertThat(this.reporter.getSpans())
			.hasSize(1);

		assertThat(this.reporter.getSpans().get(0).tags())
			.hasSize(1)
			.contains(entry("spring.profiles.active", "[prod, cloud]"));
	}

	private void reportNewSpan() {
		this.tracer.nextSpan().name("TestSpan").start().finish();
	}

	@Configuration
	@EnableAutoConfiguration
	static class TestConfig {
		@Bean
		public Sampler sampler() {
			return Sampler.ALWAYS_SAMPLE;
		}

		@Bean
		public Reporter<zipkin2.Span> reporter() {
			return new ArrayListSpanReporter();
		}
	}
}
