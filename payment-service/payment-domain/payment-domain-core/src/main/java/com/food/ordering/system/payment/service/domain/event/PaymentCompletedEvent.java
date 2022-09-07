package com.food.ordering.system.payment.service.domain.event;

import java.time.ZonedDateTime;
import java.util.Collections;

import com.food.ordering.system.payment.service.domain.entity.Payment;

public class PaymentCompletedEvent extends PaymentEvent {
    
    public PaymentCompletedEvent(
        final Payment payment,
        final ZonedDateTime createdAt) {
        super(payment, createdAt, Collections.emptyList());
    }
    
}
