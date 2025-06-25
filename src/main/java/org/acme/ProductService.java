package org.acme;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.MDC;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.kafka.api.IncomingKafkaRecordMetadata;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import lombok.extern.slf4j.Slf4j;

/**
 * Serviço responsável pela lógica de negócios relacionada aos produtos.
 * Implementa operações CRUD e validações de negócio.
 */
@ApplicationScoped
@Slf4j
public class ProductService {

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
        return productRepository.findBySkuOrName(input.getSku(), input.getName())
                .flatMap(existingProduct -> {
                    if (existingProduct != null) {
                        return Uni.createFrom().failure(
                                new ProductAlreadyExistException(input.getSku(), input.getName()));
                    }
                    return Panache.withTransaction(new Product(input.getSku(), input.getName())::persist)
                            .invoke(productPersisted -> log.info("{}", productPersisted,
                                    MDC.get("X-Trace-Id")))
                            .invoke(productPersisted -> {
                                productOutProducer.send(ProductOutputDTO.fromEntity((Product) productPersisted));
                            })
                            .map(productPersisted -> ProductOutputDTO.fromEntity((Product) productPersisted));
                });
    }

    public Uni<ProductOutputDTO> getBySku(String sku) {
        return productRepository.findBySku(sku)
                .onItem().ifNull().failWith(new ProductNotFoundException(sku))
                .map(product -> ProductOutputDTO.fromEntity((Product) product));
    }

    @Incoming("products-in")
    public Uni<Void> consume(Message<ProductOutputDTO> message) {
        var kafkaMetadata = message.getMetadata(IncomingKafkaRecordMetadata.class).orElse(null);
        String traceId = kafkaMetadata != null ? kafkaMetadata.getHeaders().lastHeader("X-Trace-Id") != null
            ? new String(kafkaMetadata.getHeaders().lastHeader("X-Trace-Id").value()) : null : null;
        log.info("{}", message.getPayload(), traceId);
        message.ack();
        return Uni.createFrom().voidItem();
    }
}
