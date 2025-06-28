package org.acme.exception;

import java.util.List;

import javax.ws.rs.core.Response;

public class ProductNotFoundException extends ProductException {
    
    public ProductNotFoundException(Long id) {
        super(
            "Produto não encontrado",
            "PRODUCT_NOT_FOUND",
            List.of(String.format("Não foi possível encontrar um produto com o ID %d", id)),
            Response.Status.NOT_FOUND
        );
    }

    public ProductNotFoundException(String sku) {
        super(
            "Produto não encontrado",
            "PRODUCT_NOT_FOUND",
            List.of(String.format("Não foi possível encontrar um produto com o SKU %s", sku)),
            Response.Status.NOT_FOUND
        );
    }
} 