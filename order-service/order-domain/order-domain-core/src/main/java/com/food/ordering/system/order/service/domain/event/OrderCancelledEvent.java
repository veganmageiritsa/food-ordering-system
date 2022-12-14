package com.food.ordering.system.order.service.domain.event;

import java.time.ZonedDateTime;

import com.food.ordering.system.order.service.domain.entity.Order;

public class OrderCancelledEvent extends OrderEvent {
    
    
    public OrderCancelledEvent(
        final Order order, final ZonedDateTime createdAt) {
        super(order, createdAt);
    }
    
    
}
