package com.food.ordering.system.order.service.domain;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.domain.event.EmptyEvent;
import com.food.ordering.system.order.service.domain.dto.message.RestaurantApprovalResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.payment.OrderCancelledRequestPaymentMessagePublisher;
import com.food.ordering.system.order.service.domain.service.OrderDomainService;
import com.food.ordering.system.saga.SagaStep;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Transactional
public class OrderApprovalSaga implements SagaStep<RestaurantApprovalResponse, EmptyEvent, OrderCancelledEvent> {
    
    private final OrderSagaHelper orderSagaHelper;
    
    private final OrderDomainService orderDomainService;
    
    private final OrderCancelledRequestPaymentMessagePublisher orderCancelledRequestPaymentMessagePublisher;
    
    public OrderApprovalSaga(
        final OrderSagaHelper orderSagaHelper,
        final OrderDomainService orderDomainService,
        final OrderCancelledRequestPaymentMessagePublisher orderCancelledRequestPaymentMessagePublisher) {
        this.orderSagaHelper = orderSagaHelper;
        this.orderDomainService = orderDomainService;
        this.orderCancelledRequestPaymentMessagePublisher = orderCancelledRequestPaymentMessagePublisher;
    }
    
    @Override
    public EmptyEvent process(final RestaurantApprovalResponse restaurantApprovalResponse) {
        String orderId = restaurantApprovalResponse.getOrderId();
        log.info("Approving order with id: {}", orderId);
        Order order = orderSagaHelper.findOrder(orderId);
        orderDomainService.approveOrder(order);
        orderSagaHelper.save(order);
        return EmptyEvent.INSTANCE;
    }
    
    @Override
    public OrderCancelledEvent rollback(final RestaurantApprovalResponse restaurantApprovalResponse) {
        String orderId = restaurantApprovalResponse.getOrderId();
        log.info("Cancelling order with id: {}", orderId);
        Order order = orderSagaHelper.findOrder(orderId);
        
        OrderCancelledEvent orderCancelledEvent = orderDomainService.cancelOrderPayment(order, restaurantApprovalResponse.getFailureMessages(),
                                                                                        orderCancelledRequestPaymentMessagePublisher);
        
        orderSagaHelper.save(order);
        return orderCancelledEvent;
    }
    
}
