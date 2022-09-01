package com.food.ordering.system.order.service.domain.event;

import java.time.ZonedDateTime;

import com.food.ordering.system.domain.event.DomainEvent;
import com.food.ordering.system.order.service.domain.entity.Order;

public class OrderCreatedEvent extends OrderEvent{
    
    public OrderCreatedEvent(final Order order, final ZonedDateTime createdAt) {
        super(order, createdAt);
    }
    
}
