package org.acme;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import io.smallrye.reactive.messaging.annotations.Blocking;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Serviço responsável pela lógica de negócios relacionada aos produtos.
 * Implementa operações CRUD e validações de negócio.
 */
@ApplicationScoped
public class ProductService {

    @Inject
    ProductRepository productRepository;

    @Inject
    ObjectMapper objectMapper;

    /**
     * Cria um novo produto.
     * 
     * @param input Dados do produto a ser criado
     * @return Produto criado convertido para DTO
     * @throws ProductValidationException se os dados do produto forem inválidos ou
     *                                    se o SKU já existir
     */
    public Uni<ProductOutputDTO> create(ProductInputDTO input) {
        return productRepository.findBySkuOrName(input.getSku(), input.getName())
                .flatMap(existingProduct -> {
                    if (existingProduct != null) {
                        return Uni.createFrom().failure(
                                new ProductAlreadyExistException(input.getSku(), input.getName()));
                    }
                    return Panache.withTransaction(new Product(input.getSku(), input.getName())::persist)
                            .map(productPersisted -> ProductOutputDTO.fromEntity((Product) productPersisted));
                });
    }

    public Uni<ProductOutputDTO> getBySku(String sku) {
        return productRepository.findBySku(sku)
                .onItem().ifNull().failWith(new ProductNotFoundException(sku))
                .map(product -> ProductOutputDTO.fromEntity((Product) product));
    }

    /**
     * Consumer Kafka para criar produtos a partir do tópico 'products'.
     * Espera mensagens JSON compatíveis com ProductInputDTO.
     */
    @Incoming("products-in")
    @Blocking
    public void consumeProduct(String message) {
        try {
            ProductInputDTO input = objectMapper.readValue(message, ProductInputDTO.class);
            // Chama o método create, mas ignora o retorno pois é void
            this.create(input).subscribe().with(
                success -> {},
                failure -> { /* logar erro se necessário */ }
            );
        } catch (Exception e) {
            // logar erro de deserialização
        }
    }
}
