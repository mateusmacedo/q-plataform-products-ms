package org.acme;

import java.util.List;

import javax.ws.rs.core.Response;

public class ProductValidationException extends ProductException {

    private final List<String> errors;

    public ProductValidationException(List<String> errors) {
        super("Erro de validação do produto", "VALIDATION_ERROR", List.of("Erro de validação do produto"),
                Response.Status.BAD_REQUEST);
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }

    @Override
    public List<String> getDetails() {
        return errors;
    }
}