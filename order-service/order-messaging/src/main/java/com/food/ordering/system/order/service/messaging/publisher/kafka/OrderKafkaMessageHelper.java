package com.food.ordering.system.order.service.messaging.publisher.kafka;

import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrderKafkaMessageHelper {
    
    public <T> ListenableFutureCallback<SendResult<String, T>> getKafkaCallback(
        final String responseTopicName,
        final T requestAvroModel,
        final String orderId,
        String requestAvroModelName) {
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
                
            }
            
            @Override
            public void onFailure(final Throwable ex) {
                log.error("Error While sending {} message {} to topic {}",
                          requestAvroModelName, requestAvroModel.toString(), responseTopicName);
            }
        };
    }
    
}
