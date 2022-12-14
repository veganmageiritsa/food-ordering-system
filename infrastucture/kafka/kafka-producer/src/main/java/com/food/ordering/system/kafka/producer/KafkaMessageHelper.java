package com.food.ordering.system.kafka.producer;

import java.util.function.BiConsumer;

import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.outbox.OutboxStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class KafkaMessageHelper {
    
    private final ObjectMapper objectMapper;
    
    public KafkaMessageHelper(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    public <T, U> ListenableFutureCallback<SendResult<String, T>> getKafkaCallback(
        final String responseTopicName,
        final T avroModel,
        U outboxMessage,
        BiConsumer<U, OutboxStatus> outboxCallback,
        final String orderId,
        String avroModelName) {
        return new ListenableFutureCallback<>() {
            
            @Override
            public void onSuccess(
                final SendResult<String, T> result) {
                final var recordMetadata = result.getRecordMetadata();
                log.info("Received response from kafka for order with id: {} Topic : {}, Partition: {} , Offset : {} , Timestamp: {}",
                         orderId,
                         recordMetadata.topic(),
                         recordMetadata.partition(),
                         recordMetadata.offset(),
                         recordMetadata.timestamp());
                outboxCallback.accept(outboxMessage, OutboxStatus.COMPLETED);
            }
            
            @Override
            public void onFailure(final Throwable ex) {
                log.error("Error While sending {} message : {} and outbox type: {}  to topic {}",
                          avroModelName, avroModel.toString(), outboxMessage.getClass().getName(), responseTopicName);
                outboxCallback.accept(outboxMessage, OutboxStatus.FAILED);
            }
        };
    }
    
    public <T> T getOrderEventPayload(final String payload, Class<T> outputType) {
        try {
            return objectMapper.readValue(payload, outputType);
        } catch (JsonProcessingException e) {
            log.error("Could not read {}} object ", outputType.getName(), e);
            throw new OrderDomainException("Could not read" + outputType.getName() + " object  ", e);
        }
    }
    
}
