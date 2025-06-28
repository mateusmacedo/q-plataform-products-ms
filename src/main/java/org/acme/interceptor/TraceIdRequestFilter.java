package org.acme.interceptor;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import io.opentelemetry.api.trace.Span;

@Provider
public class TraceIdRequestFilter implements ClientRequestFilter {
    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        String traceId = Span.current().getSpanContext().getTraceId();
        requestContext.getHeaders().add("X-Trace-Id", traceId);
    }
}

