package com.food.ordering.system.payment.service.domain.exception;

import com.food.ordering.system.domain.exception.DomainException;

public class PaymentApplicationServiceException extends DomainException {
    
    public PaymentApplicationServiceException(final String message) {
        super(message);
    }
    
    public PaymentApplicationServiceException(final String message, final Throwable cause) {
        super(message, cause);
    }
    
}
