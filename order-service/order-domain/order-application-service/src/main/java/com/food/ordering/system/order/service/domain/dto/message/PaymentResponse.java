package com.food.ordering.system.order.service.domain.dto.message;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import com.food.ordering.system.domain.valueobject.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PaymentResponse {
    
    @NotNull
    private String id;
    
    @NotNull
    private String sagaId;
    
    @NotNull
    private String orderId;
    
    @NotNull
    private String customerId;
    
    @NotNull
    private String paymentId;
    
    @NotNull
    private BigDecimal price;
    
    private Instant createdAt;
    
    private PaymentStatus paymentStatus;
    
    private List<String> failureMessages;
    
    
}
