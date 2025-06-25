package org.acme;

import java.nio.charset.StandardCharsets;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

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
        RecordHeaders headers = new RecordHeaders();
        headers.add("X-Trace-Id", traceId.getBytes(StandardCharsets.UTF_8));
        // Adiciona traceparent para interoperabilidade W3C
        // String traceparent = String.format("00-%s-0000000000000000-01", traceId.substring(0, 32));
        // headers.add("traceparent", traceparent.getBytes(StandardCharsets.UTF_8));

        emitter.send(
            Message.of(payload)
            .addMetadata(
                OutgoingKafkaRecordMetadata.builder()
                    .withHeaders(
                        headers
                    )
                    .build()
            )
        );
        return Uni.createFrom().voidItem();
    }

    private String getOrGenerate(String key) {
        Object value = MDC.get(key);
        return value != null ? value.toString() : java.util.UUID.randomUUID().toString();
    }
}