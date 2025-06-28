package org.acme.exception;

import java.util.List;

import javax.ws.rs.core.Response;

public class ProductAlreadyExistException extends ProductException {

    public ProductAlreadyExistException(String sku, String name) {
        super("Produto já existe", "PRODUCT_ALREADY_EXISTS",
                List.of(String.format("Já existe um produto com o SKU %s ou nome %s", sku, name)),
                Response.Status.CONFLICT);
    }
}
