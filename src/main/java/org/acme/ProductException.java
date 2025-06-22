package org.acme;

import java.util.List;

import javax.ws.rs.core.Response;

public class ProductException extends ApiException {

    public ProductException(String message, String errorCode, List<String> details, Response.Status status) {
        super(message, errorCode, details, status);
    }
}