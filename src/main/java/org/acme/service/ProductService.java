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
import org.jboss.logging.MDC;

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
        String traceId = MDC.get("X-Trace-Id") != null ? MDC.get("X-Trace-Id").toString() : "N/A";
        log.infof("[traceId=%s] Iniciando criação de produto: sku=%s, nome=%s", traceId, input.getSku(),
                input.getName());

        return productRepository.findBySkuOrName(input.getSku(), input.getName())
            .onItem().ifNotNull().failWith(() -> new ProductAlreadyExistException(input.getSku(), input.getName()))
            .onItem().ifNull().continueWith(() -> new Product(input.getSku(), input.getName()))
            .flatMap(product -> Panache.withTransaction(product::persist))
            .onItem().invoke(productPersisted -> {
                log.infof("[traceId=%s] Produto persistido: id=%s, sku=%s, nome=%s", traceId,
                        ((Product) productPersisted).id, ((Product) productPersisted).sku,
                        ((Product) productPersisted).name);
                productOutProducer.send(ProductOutputDTO.fromEntity((Product) productPersisted));
                log.infof("[traceId=%s] Evento enviado para Kafka: id=%s, sku=%s", traceId,
                        ((Product) productPersisted).id, ((Product) productPersisted).sku);
            })
            .map(productPersisted -> ProductOutputDTO.fromEntity((Product) productPersisted))
            .onFailure().invoke(throwable -> {
                log.errorf("[traceId=%s] Erro ao criar produto: %s", traceId, throwable.getMessage());
            });
    }

    public Uni<ProductOutputDTO> getBySku(String sku) {
        String traceId = MDC.get("X-Trace-Id") != null ? MDC.get("X-Trace-Id").toString() : "N/A";
        log.infof("[traceId=%s] Buscando produto por SKU: %s", traceId, sku);
        return productRepository.findBySku(sku)
                .onItem().ifNull().failWith(() -> {
                    log.errorf("[traceId=%s] Produto não encontrado para SKU: %s", traceId, sku);
                    return new ProductNotFoundException(sku);
                })
                .map(product -> ProductOutputDTO.fromEntity((Product) product));
    }
}
