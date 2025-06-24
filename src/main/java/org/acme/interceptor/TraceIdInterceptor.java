package org.acme.interceptor;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class TraceIdInterceptor implements ContainerRequestFilter, ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String traceId = requestContext.getHeaderString("X-Trace-Id");
        if (traceId == null) {
            traceId = java.util.UUID.randomUUID().toString();
            requestContext.getHeaders().add("X-Trace-Id", traceId);
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        String traceId = requestContext.getHeaderString("X-Trace-Id");
        if (traceId != null) {
            responseContext.getHeaders().add("X-Trace-Id", traceId);
        }
    }
}
