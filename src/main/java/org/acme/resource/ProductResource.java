package org.acme.resource;

import java.net.URI;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.acme.exception.ProductValidationException;
import org.acme.dto.ProductErrorDTO;
import org.acme.dto.ProductInputDTO;
import org.acme.dto.ProductOutputDTO;
import org.acme.service.ProductService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.MDC;

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
            @APIResponse(responseCode = "201", description = "Produto criado com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductErrorDTO.class))),
            @APIResponse(responseCode = "400", description = "Dados do produto inválidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductErrorDTO.class))),
            @APIResponse(responseCode = "409", description = "SKU já existe ou nome já existe", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductErrorDTO.class))),
            @APIResponse(responseCode = "500", description = "Erro interno do servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductErrorDTO.class)))
    })
    public Uni<Response> createProduct(ProductInputDTO product) {
        String traceId = MDC.get("X-Trace-Id") != null ? MDC.get("X-Trace-Id").toString() : "N/A";
        log.info(String.format("[traceId=%s] Recebida requisição para criar produto: sku=%s, nome=%s", traceId, product.getSku(), product.getName()));
        Set<ConstraintViolation<ProductInputDTO>> violations = validator.validate(product);
        if (!violations.isEmpty()) {
            log.warn(String.format("[traceId=%s] Falha de validação ao criar produto: %s", traceId, violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(", "))));
            return Uni.createFrom().failure(new ProductValidationException(violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.toList())));
        }
        return productService.create(product)
                .onItem().transform(i -> {
                    log.info(String.format("[traceId=%s] Produto criado com sucesso: sku=%s, nome=%s", traceId, i.getSku(), i.getName()));
                    return Response.created(
                        URI.create("/products/" + i.getSku()))
                        .entity(i).build();
                })
                .invoke(MDC::clear);
    }

    @GET
    @Path("{sku}")
    @Operation(summary = "Obtém um produto por SKU", description = "Obtém um produto por SKU")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Produto encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductOutputDTO.class))),
            @APIResponse(responseCode = "404", description = "Produto não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductErrorDTO.class)))
    })
    public Uni<Response> getProductBySku(@PathParam("sku") String sku) {
        String traceId = MDC.get("X-Trace-Id") != null ? MDC.get("X-Trace-Id").toString() : "N/A";
        log.info(String.format("[traceId=%s] Recebida requisição para buscar produto por SKU: %s", traceId, sku));
        return productService.getBySku(sku)
                .onItem().transform(i -> {
                    log.info(String.format("[traceId=%s] Produto encontrado: sku=%s, nome=%s", traceId, i.getSku(), i.getName()));
                    return Response.ok(i).build();
                });
    }
}
