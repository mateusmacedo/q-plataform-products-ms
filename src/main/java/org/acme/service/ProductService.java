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
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.kafka.api.IncomingKafkaRecordMetadata;

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
                .flatMap(existingProduct -> {
                    if (existingProduct != null) {
                        log.errorf("[traceId=%s] Produto já existe: sku=%s, nome=%s", traceId, input.getSku(),
                                input.getName());
                        return Uni.createFrom().failure(
                                new ProductAlreadyExistException(input.getSku(), input.getName()));
                    }
                    return Panache.withTransaction(new Product(input.getSku(), input.getName())::persist)
                            .invoke(productPersisted -> log.infof(
                                    "[traceId=%s] Produto persistido: id=%s, sku=%s, nome=%s", traceId,
                                    ((Product) productPersisted).id, ((Product) productPersisted).sku,
                                    ((Product) productPersisted).name))
                            .invoke(productPersisted -> {
                                productOutProducer.send(ProductOutputDTO.fromEntity((Product) productPersisted));
                                log.infof("[traceId=%s] Evento enviado para Kafka: id=%s, sku=%s", traceId,
                                        ((Product) productPersisted).id, ((Product) productPersisted).sku);
                            })
                            .map(productPersisted -> ProductOutputDTO.fromEntity((Product) productPersisted));
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

    @Incoming("products-in")
    public Uni<Void> consume(Message<String> message) {
        var kafkaMetadata = message.getMetadata(IncomingKafkaRecordMetadata.class).orElse(null);
        String traceId = kafkaMetadata != null && kafkaMetadata.getHeaders().lastHeader("X-Trace-Id") != null
                ? new String(kafkaMetadata.getHeaders().lastHeader("X-Trace-Id").value())
                : "N/A";
        try {
            ProductOutputDTO payload = objectMapper.readValue(message.getPayload(), ProductOutputDTO.class);
            log.infof("[traceId=%s] Mensagem recebida do Kafka: id=%s, sku=%s, nome=%s", traceId, payload.getId(), payload.getSku(), payload.getName());
            message.ack();
        } catch (Exception e) {
            log.errorf("[traceId=%s] Falha ao processar mensagem do Kafka: %s", traceId, e.getMessage());
            return Uni.createFrom().failure(e);
        }
        return Uni.createFrom().voidItem();
    }
}
