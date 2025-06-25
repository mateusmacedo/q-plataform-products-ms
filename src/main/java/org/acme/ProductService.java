package org.acme;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;
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
        log.infof("[traceId=%s] Iniciando criação de produto: sku=%s, nome=%s", traceId, input.getSku(), input.getName());
        return productRepository.findBySkuOrName(input.getSku(), input.getName())
                .flatMap(existingProduct -> {
                    if (existingProduct != null) {
                        log.errorf("[traceId=%s] Produto já existe: sku=%s, nome=%s", traceId, input.getSku(), input.getName());
                        return Uni.createFrom().failure(
                                new ProductAlreadyExistException(input.getSku(), input.getName()));
                    }
                    return Panache.withTransaction(new Product(input.getSku(), input.getName())::persist)
                            .invoke(productPersisted -> log.infof("[traceId=%s] Produto persistido: id=%s, sku=%s, nome=%s", traceId, ((Product)productPersisted).id, ((Product)productPersisted).sku, ((Product)productPersisted).name))
                            .invoke(productPersisted -> {
                                productOutProducer.send(ProductOutputDTO.fromEntity((Product) productPersisted));
                                log.infof("[traceId=%s] Evento enviado para Kafka: id=%s, sku=%s", traceId, ((Product)productPersisted).id, ((Product)productPersisted).sku);
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
    public Uni<Void> consume(Message<ProductOutputDTO> message) {
        var kafkaMetadata = message.getMetadata(IncomingKafkaRecordMetadata.class).orElse(null);
        String traceId = kafkaMetadata != null && kafkaMetadata.getHeaders().lastHeader("X-Trace-Id") != null
            ? new String(kafkaMetadata.getHeaders().lastHeader("X-Trace-Id").value()) : "N/A";
        ProductOutputDTO payload = message.getPayload();
        log.infof("[traceId=%s] Mensagem recebida do Kafka: id=%s, sku=%s, nome=%s", traceId, payload.getId(), payload.getSku(), payload.getName());
        message.ack();
        return Uni.createFrom().voidItem();
    }
}
