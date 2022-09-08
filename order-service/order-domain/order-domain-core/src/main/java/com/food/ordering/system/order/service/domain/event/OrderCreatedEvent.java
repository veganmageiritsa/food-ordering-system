package com.food.ordering.system.order.service.domain.event;

import java.time.ZonedDateTime;

import com.food.ordering.system.domain.event.publisher.DomainEventPublisher;
import com.food.ordering.system.order.service.domain.entity.Order;

public class OrderCreatedEvent extends OrderEvent {
    
    private final DomainEventPublisher<OrderCreatedEvent> orderCreatedEventDomainEventPublisher;
    
    public OrderCreatedEvent(
        final Order order, final ZonedDateTime createdAt,
        final DomainEventPublisher<OrderCreatedEvent> orderCreatedEventDomainEventPublisher) {
        super(order, createdAt);
        this.orderCreatedEventDomainEventPublisher = orderCreatedEventDomainEventPublisher;
    }
    
    @Override
    public void fire() {
        orderCreatedEventDomainEventPublisher.publish(this);
    }
    
}
