package org.acme.interceptor;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class CorrelationIdInterceptor implements ContainerRequestFilter, ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String correlationId = requestContext.getHeaderString("X-Correlation-Id");
        if (correlationId == null) {
            correlationId = java.util.UUID.randomUUID().toString();
            requestContext.getHeaders().add("X-Correlation-Id", correlationId);
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        String correlationId = requestContext.getHeaderString("X-Correlation-Id");
        if (correlationId != null) {
            responseContext.getHeaders().add("X-Correlation-Id", correlationId);
        }
    }
}
