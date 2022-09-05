package com.food.ordering.system.kafka.producer.exception;

public class KafkaProducerException extends RuntimeException {
    
    public KafkaProducerException(final String message) {
        super(message);
    }
    
}
