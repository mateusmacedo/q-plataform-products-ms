package org.acme;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;

/**
 * Serviço responsável pela lógica de negócios relacionada aos produtos.
 * Implementa operações CRUD e validações de negócio.
 */
@ApplicationScoped
public class ProductService {

    @Inject
    ProductRepository productRepository;

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
}
