package com.food.ordering.system.order.service.domain;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.payment.OrderCreatedPaymentRequestMessagePublisher;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrderCreateCommandHandler {
    
    private final OrderCreateHelper orderCreateHelper;
    
    private final OrderDataMapper orderDataMapper;
    
    private final OrderCreatedPaymentRequestMessagePublisher publisher;
    public OrderCreateCommandHandler(
        final OrderCreateHelper orderCreateHelper,
        final OrderDataMapper orderDataMapper,
        final OrderCreatedPaymentRequestMessagePublisher publisher) {
        this.orderCreateHelper = orderCreateHelper;
        this.orderDataMapper = orderDataMapper;
        this.publisher = publisher;
    }
    
    
    public CreateOrderResponse createOrder(CreateOrderCommand createOrderCommand) {
        OrderCreatedEvent orderCreatedEvent = orderCreateHelper.createOrder(createOrderCommand);
        publisher.publish(orderCreatedEvent);
        return orderDataMapper.orderToCreateOrderResponse(orderCreatedEvent.getOrder(), "Order Created Successfully");
        
    }
    
    
    
}
