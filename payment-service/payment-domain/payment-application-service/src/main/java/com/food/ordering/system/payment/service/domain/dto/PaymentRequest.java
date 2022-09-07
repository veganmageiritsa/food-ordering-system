package com.food.ordering.system.payment.service.domain.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.food.ordering.system.domain.valueobject.PaymentOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PaymentRequest {
    
    private String id;
    
    private String sagaId;
    
    private String orderId;
    
    private String customerId;
    
    private BigDecimal price;
    
    private Instant createdAt;
    
    private PaymentOrderStatus paymentOrderStatus;
    
    public void setPaymentOrderStatus(final PaymentOrderStatus paymentOrderStatus) {
        this.paymentOrderStatus = paymentOrderStatus;
    }
    
}
