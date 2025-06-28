package org.acme.producer;

import java.nio.charset.StandardCharsets;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.acme.dto.ProductOutputDTO;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.MDC;

import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class ProductOutProducer {

    @Inject
    @Channel("products-out")
    Emitter<ProductOutputDTO> emitter;

    public Uni<Void> send(ProductOutputDTO payload) {
        String traceId = getOrGenerate("X-Trace-Id");
        log.info(String.format("[traceId=%s] Enviando mensagem para Kafka: id=%s, sku=%s, nome=%s", traceId, payload.getId(), payload.getSku(), payload.getName()));
        try {
            RecordHeaders headers = new RecordHeaders();
            headers.add("X-Trace-Id", traceId.getBytes(StandardCharsets.UTF_8));
            emitter.send(
                Message.of(payload)
                .addMetadata(
                    OutgoingKafkaRecordMetadata.builder()
                        .withHeaders(headers)
                        .build()
                )
            );
            log.info(String.format("[traceId=%s] Mensagem enviada com sucesso para Kafka: id=%s, sku=%s", traceId, payload.getId(), payload.getSku()));
        } catch (Exception e) {
            log.error(String.format("[traceId=%s] Falha ao enviar mensagem para Kafka: id=%s, sku=%s, erro=%s", traceId, payload.getId(), payload.getSku(), e.getMessage()), e);
            return Uni.createFrom().failure(e);
        }
        return Uni.createFrom().voidItem();
    }

    private String getOrGenerate(String key) {
        Object value = MDC.get(key);
        return value != null ? value.toString() : java.util.UUID.randomUUID().toString();
    }
}