package com.mycompany.myframework.tracing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import org.springframework.http.HttpMessage;

/**
 * Utility methods to modify a response for tracing
 */
public final class TracingResponseModifierUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(TracingResponseModifierUtils.class);
	public static final String TRACE_HEADER_TRACE_ID = "traceId";
	public static final String TRACE_HEADER_SPAN_EXPORTABLE = "spanExportable";

	private TracingResponseModifierUtils() {
	}

	/**
	 * Adds tracing headers to the response. Clients can use them to lookup correlated logs or, if exported, can lookup traces in Zipkin.
	 * @param httpMessage The {@link HttpMessage}, common to both the servlet & reactive stacks
	 */
	public static void addTracingHeaders(HttpMessage httpMessage) {
		String log = "Setting {} http header";
		String traceId = MDC.get(TRACE_HEADER_TRACE_ID);
		String spanExportable = MDC.get(TRACE_HEADER_SPAN_EXPORTABLE);

		if (traceId != null) {
			LOGGER.debug(log, TRACE_HEADER_TRACE_ID);
			httpMessage.getHeaders().add(TRACE_HEADER_TRACE_ID, traceId);
		}
		else {
			LOGGER.warn("No {} found, verify tracing setup", TRACE_HEADER_TRACE_ID);
		}

		if (spanExportable != null) {
			LOGGER.debug(log, TRACE_HEADER_SPAN_EXPORTABLE);
			httpMessage.getHeaders().add(TRACE_HEADER_SPAN_EXPORTABLE, spanExportable);
		}
	}
}
