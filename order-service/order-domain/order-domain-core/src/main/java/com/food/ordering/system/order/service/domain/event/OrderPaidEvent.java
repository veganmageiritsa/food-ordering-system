package com.food.ordering.system.order.service.domain.event;

import java.time.ZonedDateTime;

import com.food.ordering.system.domain.event.publisher.DomainEventPublisher;
import com.food.ordering.system.order.service.domain.entity.Order;

public class OrderPaidEvent extends OrderEvent {
    
    private final DomainEventPublisher<OrderPaidEvent> orderPaidEventDomainEventPublisher;
    
    public OrderPaidEvent(
        final Order order, final ZonedDateTime createdAt,
        final DomainEventPublisher<OrderPaidEvent> orderCreatedEventDomainEventPublisher) {
        super(order, createdAt);
        this.orderPaidEventDomainEventPublisher = orderCreatedEventDomainEventPublisher;
    }
    
    @Override
    public void fire() {
        orderPaidEventDomainEventPublisher.publish(this);
    }
    
}
