package com.food.ordering.system.kafka.producer;

import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class KafkaMessageHelper {
    
    public <T> ListenableFutureCallback<SendResult<String, T>> getKafkaCallback(
        final String responseTopicName,
        final T avroModel,
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
                
            }
            
            @Override
            public void onFailure(final Throwable ex) {
                log.error("Error While sending {} message {} to topic {}",
                          avroModelName, avroModel.toString(), responseTopicName);
            }
        };
    }
    
}
