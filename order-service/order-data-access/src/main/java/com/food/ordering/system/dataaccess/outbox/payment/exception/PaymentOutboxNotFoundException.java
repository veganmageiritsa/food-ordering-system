package com.food.ordering.system.dataaccess.outbox.payment.exception;

public class PaymentOutboxNotFoundException extends RuntimeException {
    
    public PaymentOutboxNotFoundException(String message) {
        super(message);
    }
    
}
