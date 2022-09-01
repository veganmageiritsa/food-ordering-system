package com.food.ordering.system.order.service.domain.dto.message;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.food.ordering.system.domain.valueobject.OrderApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RestaurantApprovalResponse {
    @NotNull
    private String id;
    
    @NotNull
    private String sagaId;
    
    @NotNull
    private String orderId;
    
    @NotNull
    private String restaurantId;
    
    @NotNull
    private BigDecimal price;
    
    private Instant createdAt;
    private OrderApprovalStatus orderApprovalStatus;
    private List<String> failureMessages;
}
