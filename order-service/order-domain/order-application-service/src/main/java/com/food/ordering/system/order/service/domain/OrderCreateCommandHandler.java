package com.food.ordering.system.order.service.domain;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.domain.valueobject.OrderStatus;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.outbox.scheduler.payment.PaymentOutboxHelper;
import com.food.ordering.system.outbox.OutboxStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrderCreateCommandHandler {
    
    private final OrderCreateHelper orderCreateHelper;
    
    private final OrderDataMapper orderDataMapper;
    
    private final PaymentOutboxHelper paymentOutboxHelper;
    
    private final OrderSagaHelper orderSagaHelper;
    
    public OrderCreateCommandHandler(
        final OrderCreateHelper orderCreateHelper,
        final OrderDataMapper orderDataMapper,
        final PaymentOutboxHelper paymentOutboxHelper,
        final OrderSagaHelper orderSagaHelper) {
        this.orderCreateHelper = orderCreateHelper;
        this.orderDataMapper = orderDataMapper;
        this.paymentOutboxHelper = paymentOutboxHelper;
        this.orderSagaHelper = orderSagaHelper;
    }
    
    
    @Transactional
    public CreateOrderResponse createOrder(CreateOrderCommand createOrderCommand) {
        OrderCreatedEvent orderCreatedEvent = orderCreateHelper.persistOrder(createOrderCommand);
        OrderStatus orderStatus = orderCreatedEvent.getOrder().getOrderStatus();
        
        log.info("Order is created with id: {}", orderCreatedEvent.getOrder().getId().getValue());
        
        paymentOutboxHelper.savePaymentOutboxMessage(
            orderDataMapper.orderCreatedEventToOrderPaymentEventPayload(orderCreatedEvent),
            orderStatus,
            orderSagaHelper.orderStatusToSagaStatus(orderStatus),
            OutboxStatus.STARTED,
            UUID.randomUUID()
        );
        
        log.info("Returning CreateOrderResponse with id: {}", orderCreatedEvent.getOrder().getId());
        
        return orderDataMapper.orderToCreateOrderResponse(orderCreatedEvent.getOrder(), "Order Created Successfully");
        
    }
    
    
    
}
