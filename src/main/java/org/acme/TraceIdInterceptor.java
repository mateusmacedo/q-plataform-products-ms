package org.acme;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import org.jboss.logging.MDC;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;

import java.io.IOException;

@Provider
public class TraceIdInterceptor implements ContainerRequestFilter, ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String traceId = requestContext.getHeaderString("X-Trace-Id");
        if (traceId == null) {
            Span span = Span.current();
            SpanContext spanContext = span.getSpanContext();
            traceId = spanContext.getTraceId();
            requestContext.getHeaders().add("X-Trace-Id", traceId);
        }
        MDC.put("X-Trace-Id", traceId);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        String traceId = requestContext.getHeaderString("X-Trace-Id");
        if (traceId != null) {
            responseContext.getHeaders().add("X-Trace-Id", traceId);
        }
    }
}
