package com.food.ordering.system.order.service.domain;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.domain.event.EmptyEvent;
import com.food.ordering.system.order.service.domain.dto.message.PaymentResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.restaurantapproval.OrderPaidRestaurantRequestMessagePublisher;
import com.food.ordering.system.order.service.domain.service.OrderDomainService;
import com.food.ordering.system.saga.SagaStep;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Transactional
public class OrderPaymentSaga implements SagaStep<PaymentResponse, OrderPaidEvent, EmptyEvent> {
    
    private final OrderDomainService orderDomainService;
    
    private final OrderPaidRestaurantRequestMessagePublisher orderPaidRestaurantRequestMessagePublisher;
    
    private final OrderSagaHelper orderSagaHelper;
    
    public OrderPaymentSaga(
        final OrderDomainService orderDomainService,
        final OrderPaidRestaurantRequestMessagePublisher orderPaidRestaurantRequestMessagePublisher,
        final OrderSagaHelper orderSagaHelper) {
        this.orderDomainService = orderDomainService;
        this.orderPaidRestaurantRequestMessagePublisher = orderPaidRestaurantRequestMessagePublisher;
        this.orderSagaHelper = orderSagaHelper;
    }
    
    @Override
    public OrderPaidEvent process(final PaymentResponse paymentResponse) {
        String orderId = paymentResponse.getOrderId();
        log.info("Completing payment for order with id: {}", orderId);
        Order order = orderSagaHelper.findOrder(orderId);
        OrderPaidEvent orderPaidEvent = orderDomainService.payOrder(order, orderPaidRestaurantRequestMessagePublisher);
        orderSagaHelper.save(order);
        return orderPaidEvent;
    }
    
    
    @Override
    public EmptyEvent rollback(final PaymentResponse paymentResponse) {
        String orderId = paymentResponse.getOrderId();
        log.info("Cancelling payment for order with id: {}", orderId);
        
        Order order = orderSagaHelper.findOrder(orderId);
        orderDomainService.cancelOrder(order, paymentResponse.getFailureMessages());
        orderSagaHelper.save(order);
        log.info("order with id: {} is cancelled ", order.getId().getValue());
        
        return EmptyEvent.INSTANCE;
    }
    
}
