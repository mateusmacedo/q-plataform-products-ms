package org.acme.producer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.acme.dto.ProductOutputDTO;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;

@ApplicationScoped
public class ProductOutProducer {
    Logger log = Logger.getLogger(ProductOutProducer.class);

    @Inject
    @Channel("products-out")
    Emitter<ProductOutputDTO> emitter;

    public Uni<Void> send(ProductOutputDTO payload) {
        return Uni.createFrom().item(() -> {
            return Message.of(payload)
                    .addMetadata(
                            OutgoingKafkaRecordMetadata.builder()
                                    .withHeaders(new RecordHeaders())
                                    .build());
        })
                .invoke(message -> emitter.send(message))
                .invoke(() -> log.infof("Enviando produto: %s", payload))
                .replaceWithVoid()
                .onFailure().invoke(throwable -> log.errorf("Erro ao enviar produto: %s", throwable.getMessage()));
    }
}