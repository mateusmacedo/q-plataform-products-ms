package org.acme;

import java.net.URI;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

/**
 * Recurso REST para gerenciamento de produtos.
 * Fornece endpoints para criar, ler, atualizar e excluir produtos.
 */
@Path("products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Produtos", description = "API para gerenciamento de produtos")
@Slf4j
@ApplicationScoped
public class ProductResource {

    @Inject
    Validator validator;

    @Inject
    ProductService productService;

    /**
     * Cria um novo produto.
     * 
     * @param input Dados do produto a ser criado
     * @return Produto criado
     */
    @POST
    @Operation(summary = "Cria um novo produto", description = "Cria um novo produto com os dados fornecidos")
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Produto criado com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductOutputDTO.class))),
            @APIResponse(responseCode = "400", description = "Dados do produto inválidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductErroDTO.class))),
            @APIResponse(responseCode = "409", description = "SKU já existe ou nome já existe", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductErroDTO.class))),
            @APIResponse(responseCode = "500", description = "Erro interno do servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductErroDTO.class)))
    })
    public Uni<Response> createProduct(ProductInputDTO product) {

        Set<ConstraintViolation<ProductInputDTO>> violations = validator.validate(product);
        if (!violations.isEmpty()) {
            return Uni.createFrom().failure(new ProductValidationException(violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.toList())));
        } // TODO: Abstrair encapsulando a validação em um método

        return productService.create(product)
                .onItem().transform(i -> Response.created(
                        URI.create("/products/" + i.getSku()))
                        .entity(i).build());
    }

    @GET
    @Path("{sku}")
    @Operation(summary = "Obtém um produto por SKU", description = "Obtém um produto por SKU")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Produto encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductOutputDTO.class))),
            @APIResponse(responseCode = "404", description = "Produto não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductErroDTO.class)))
    })
    public Uni<Response> getProductBySku(@PathParam("sku") String sku) {
        return productService.getBySku(sku)
                .onItem().transform(i -> Response.ok(i).build());
    }
}
