package com.food.ordering.system.payment.service.domain.event;

import java.time.ZonedDateTime;
import java.util.List;

import com.food.ordering.system.payment.service.domain.entity.Payment;

public class PaymentFailedEvent extends PaymentEvent {
    
    public PaymentFailedEvent(
        final Payment payment,
        final ZonedDateTime createdAt,
        final List<String> failureMessages) {
        super(payment, createdAt, failureMessages);
    }
    
    
}
