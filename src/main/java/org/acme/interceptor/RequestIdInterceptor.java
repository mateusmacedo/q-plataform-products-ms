package org.acme.interceptor;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class RequestIdInterceptor implements ContainerRequestFilter, ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String requestId = requestContext.getHeaderString("X-Request-Id");
        if (requestId == null) {
            requestId = java.util.UUID.randomUUID().toString();
            requestContext.getHeaders().add("X-Request-Id", requestId);
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        String requestId = requestContext.getHeaderString("X-Request-Id");
        if (requestId != null) {
            responseContext.getHeaders().add("X-Request-Id", requestId);
        }
    }
}
