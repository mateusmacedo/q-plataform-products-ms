package org.acme;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Provider
public class ProductExceptionMapper {

    @Provider
    public static class ProductNotFoundExceptionMapper implements ExceptionMapper<ProductNotFoundException> {
        @Override
        public Response toResponse(ProductNotFoundException exception) {

            ObjectNode errorJson = createErrorJson(exception);
            errorJson.put("errorCode", "PRODUCT_NOT_FOUND");
            ArrayNode details = errorJson.putArray("details");
            exception.getDetails().forEach(detail -> details.add(detail));

            return Response.status(404)
                    .entity(errorJson)
                    .build();
        }
    }

    @Provider
    public static class ProductValidationExceptionMapper implements ExceptionMapper<ProductValidationException> {
        @Override
        public Response toResponse(ProductValidationException exception) {

            ObjectNode errorJson = createErrorJson(exception);
            errorJson.put("errorCode", "PRODUCT_VALIDATION_ERROR");
            ArrayNode validationErrors = errorJson.putArray("details");
            exception.getErrors().forEach(error -> validationErrors.add(error));

            return Response.status(400)
                    .entity(errorJson)
                    .build();
        }
    }

    @Provider
    public static class ProductAlreadyExistExceptionMapper implements ExceptionMapper<ProductAlreadyExistException> {
        @Override
        public Response toResponse(ProductAlreadyExistException exception) {
            ObjectNode errorJson = createErrorJson(exception);  
            errorJson.put("errorCode", "PRODUCT_ALREADY_EXISTS");
            ArrayNode details = errorJson.putArray("details");
            exception.getDetails().forEach(detail -> details.add(detail));

            return Response.status(409)
                    .entity(errorJson)
                    .build();
        }
    }

    @Provider
    public static class InternalServerErrorExceptionMapper implements ExceptionMapper<Exception> {
        @Override
        public Response toResponse(Exception exception) {
            ObjectNode errorJson = createErrorJson(exception);
            errorJson.put("errorCode", "INTERNAL_SERVER_ERROR");
            ArrayNode details = errorJson.putArray("details");
            details.add(exception.getMessage());

            return Response.status(500)
                    .entity(errorJson)
                    .build();
        }
    }

    private static ObjectNode createErrorJson(Exception exception) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode errorJson = mapper.createObjectNode();
        errorJson.put("message", exception.getMessage());
        return errorJson;
    }
}