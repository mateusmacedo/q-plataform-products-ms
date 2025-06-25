package org.acme;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Provider
public class ProductExceptionMapper {

    @Provider
    public static class ProductNotFoundExceptionMapper implements ExceptionMapper<ProductNotFoundException> {
        @Override
        public Response toResponse(ProductNotFoundException exception) {
            ProductErrorDTO error = ProductErrorDTO.fromException(exception);
            return Response.status(404)
                    .entity(error)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }

    @Provider
    public static class ProductValidationExceptionMapper implements ExceptionMapper<ProductValidationException> {
        @Override
        public Response toResponse(ProductValidationException exception) {
            ProductErrorDTO error = ProductErrorDTO.fromException(exception);
            return Response.status(400)
                    .entity(error)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }

    @Provider
    public static class ProductAlreadyExistExceptionMapper implements ExceptionMapper<ProductAlreadyExistException> {
        @Override
        public Response toResponse(ProductAlreadyExistException exception) {
            ProductErrorDTO error = ProductErrorDTO.fromException(exception);
            return Response.status(409)
                    .entity(error)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }

    @Provider
    public static class InternalServerErrorExceptionMapper implements ExceptionMapper<Exception> {
        @Override
        public Response toResponse(Exception exception) {
            ProductErrorDTO error = new ProductErrorDTO(
                "Erro interno do servidor",
                "INTERNAL_SERVER_ERROR",
                java.util.List.of(exception.getMessage())
            );
            exception.printStackTrace(); // Logar stacktrace para troubleshooting
            return Response.status(500)
                    .entity(error)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }
}