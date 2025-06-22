package org.acme;

import java.time.LocalDateTime;
import java.util.Random;

import javax.enterprise.context.ApplicationScoped;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
@ApplicationScoped
@Slf4j
public class ProductResource {

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
            @APIResponse(responseCode = "400", description = "Dados do produto inválidos"),
            @APIResponse(responseCode = "422", description = "SKU já existe")
    })
    public Uni<ProductOutputDTO> createProduct(@Valid ProductInputDTO product) {
        return Uni.createFrom().item(new ProductOutputDTO(
            new Random().nextLong(),
            product.getSku(),
            product.getName(),
            LocalDateTime.now(),
            LocalDateTime.now()
        ))
        .invoke(productOutputDTO -> log.info("Produto criado: {}", productOutputDTO));
    }
}
