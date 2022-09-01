package com.food.ordering.system.order.service.domain.exception;

public class OrderDomainException extends DomainException {
    
    public OrderDomainException(final String message) {
        super(message);
    }
    
    public OrderDomainException(final String message, final Throwable cause) {
        super(message, cause);
    }
    
}
