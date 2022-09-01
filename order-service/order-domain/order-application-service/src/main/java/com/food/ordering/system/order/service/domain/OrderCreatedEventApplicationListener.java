package com.food.ordering.system.order.service.domain;


import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.payment.OrderCreatedPaymentRequestMessagePublisher;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrderCreatedEventApplicationListener {
private final OrderCreatedPaymentRequestMessagePublisher publisher;
    
    public OrderCreatedEventApplicationListener(
        final OrderCreatedPaymentRequestMessagePublisher publisher) {
        this.publisher = publisher;
    }
    // will only be invoked after transaction is commited
    @TransactionalEventListener
    public void process(OrderCreatedEvent orderCreatedEvent){
         publisher.publish(orderCreatedEvent);
    }
}
