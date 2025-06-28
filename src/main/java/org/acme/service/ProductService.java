package org.acme.service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.acme.dto.ProductInputDTO;
import org.acme.dto.ProductOutputDTO;
import org.acme.exception.ProductAlreadyExistException;
import org.acme.exception.ProductNotFoundException;
import org.acme.exception.ProductValidationException;
import org.acme.model.Product;
import org.acme.producer.ProductOutProducer;
import org.acme.repository.ProductRepository;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;

/**
 * Serviço responsável pela lógica de negócios relacionada aos produtos.
 * Implementa operações CRUD e validações de negócio.
 */
@ApplicationScoped
public class ProductService {

    Logger log = Logger.getLogger(ProductService.class);

    @Inject
    ProductRepository productRepository;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    ProductOutProducer productOutProducer;

    /**
     * Cria um novo produto.
     * 
     * @param input Dados do produto a ser criado
     * @return Produto criado convertido para DTO
     * @throws ProductValidationException se os dados do produto forem inválidos ou
     *                                    se o SKU já existir
     */
    public Uni<ProductOutputDTO> create(ProductInputDTO input) {
        log.infof("Iniciando criação de produto: sku=%s, nome=%s", input.getSku(),
                input.getName());

        return productRepository.findBySkuOrName(input.getSku(), input.getName())
                .onItem().ifNotNull().failWith(() -> new ProductAlreadyExistException(input.getSku(), input.getName()))
                .onItem().ifNull().continueWith(() -> new Product(input.getSku(), input.getName()))
                .flatMap(product -> Panache.withTransaction(product::persist))
                .onItem().invoke(productPersisted -> {
                    log.infof("Produto persistido: id=%s, sku=%s, nome=%s",
                            ((Product) productPersisted).id, ((Product) productPersisted).sku,
                            ((Product) productPersisted).name);
                })
                .invoke(productPersisted -> productOutProducer
                        .send(ProductOutputDTO.fromEntity((Product) productPersisted))
                        .subscribe().with(
                                unused -> log.infof("Evento enviado com sucesso: id=%s, sku=%s",
                                        ((Product) productPersisted).id, ((Product) productPersisted).sku),
                                throwable -> {
                                    log.errorf("Erro ao enviar evento: %s", throwable.getMessage());
                                    throw new RuntimeException("Erro ao enviar evento para Kafka", throwable);
                                }))
                .map(productPersisted -> ProductOutputDTO.fromEntity((Product) productPersisted))
                .onFailure().invoke(throwable -> {
                    log.errorf("Erro ao criar produto: %s", throwable.getMessage());
                });
    }

    public Uni<ProductOutputDTO> getBySku(String sku) {
        log.infof("Buscando produto por SKU: %s", sku);
        return productRepository.findBySku(sku)
                .onItem().ifNull().failWith(() -> {
                    log.errorf("Produto não encontrado para SKU: %s", sku);
                    return new ProductNotFoundException(sku);
                })
                .map(product -> ProductOutputDTO.fromEntity((Product) product));
    }
}
