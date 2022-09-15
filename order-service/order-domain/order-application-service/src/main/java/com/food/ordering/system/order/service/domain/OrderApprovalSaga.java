package com.food.ordering.system.order.service.domain;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.food.ordering.system.domain.DomainConstants.UTC;
import com.food.ordering.system.domain.valueobject.OrderStatus;
import com.food.ordering.system.order.service.domain.dto.message.RestaurantApprovalResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.outbox.scheduler.approval.ApprovalOutboxHelper;
import com.food.ordering.system.order.service.domain.outbox.scheduler.payment.PaymentOutboxHelper;
import com.food.ordering.system.order.service.domain.service.OrderDomainService;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import com.food.ordering.system.saga.SagaStep;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Transactional
public class OrderApprovalSaga implements SagaStep<RestaurantApprovalResponse> {
    
    private final OrderSagaHelper orderSagaHelper;
    
    private final OrderDomainService orderDomainService;
    
    private final PaymentOutboxHelper paymentOutboxHelper;
    
    private final ApprovalOutboxHelper approvalOutboxHelper;
    
    private final OrderDataMapper orderDataMapper;
    
    public OrderApprovalSaga(
        final OrderSagaHelper orderSagaHelper,
        final OrderDomainService orderDomainService,
        final PaymentOutboxHelper paymentOutboxHelper,
        final ApprovalOutboxHelper approvalOutboxHelper,
        final OrderDataMapper orderDataMapper) {
        this.orderSagaHelper = orderSagaHelper;
        this.orderDomainService = orderDomainService;
        this.paymentOutboxHelper = paymentOutboxHelper;
        this.approvalOutboxHelper = approvalOutboxHelper;
        this.orderDataMapper = orderDataMapper;
    }
    
    @Override
    public void process(final RestaurantApprovalResponse restaurantApprovalResponse) {
        String orderId = restaurantApprovalResponse.getOrderId();
        
        Optional<OrderApprovalOutboxMessage> orderApprovalOutboxMessageResponse =
            approvalOutboxHelper.getApprovalOutboxMessageBySagaIdAndSagaStatus(UUID.fromString(restaurantApprovalResponse.getSagaId()),
                                                                               SagaStatus.PROCESSING);
        
        if (orderApprovalOutboxMessageResponse.isEmpty()) {
            log.info("An outbox message with saga id: {} is already processed", restaurantApprovalResponse.getSagaId());
            return;
        }
        OrderApprovalOutboxMessage orderApprovalOutboxMessage = orderApprovalOutboxMessageResponse.get();
        Order order = approveOrder(orderId);
        SagaStatus sagaStatus = orderSagaHelper.orderStatusToSagaStatus(order.getOrderStatus());
        updateApprovalOutboxMessage(orderApprovalOutboxMessage, order.getOrderStatus(), sagaStatus);
        approvalOutboxHelper.save(orderApprovalOutboxMessage);
        OrderPaymentOutboxMessage updatedPaymentOutboxMessage = getUpdatedPaymentOutboxMessage(restaurantApprovalResponse.getSagaId(), order.getOrderStatus(),
                                                                                               sagaStatus);
        paymentOutboxHelper.save(updatedPaymentOutboxMessage);
        
        log.info("Order with id: {} is approved", order.getId().getValue());
        
    }
    
    
    @Override
    public void rollback(final RestaurantApprovalResponse restaurantApprovalResponse) {
        Optional<OrderApprovalOutboxMessage> orderApprovalOutboxMessageResponse =
            approvalOutboxHelper.getApprovalOutboxMessageBySagaIdAndSagaStatus(UUID.fromString(restaurantApprovalResponse.getSagaId()),
                                                                               SagaStatus.PROCESSING);
        
        if (orderApprovalOutboxMessageResponse.isEmpty()) {
            log.info("An outbox message with saga id: {} is already roll backed", restaurantApprovalResponse.getSagaId());
            return;
        }
        OrderApprovalOutboxMessage orderApprovalOutboxMessage = orderApprovalOutboxMessageResponse.get();
        
        OrderCancelledEvent orderCancelledEvent = rollBackOrder(restaurantApprovalResponse);
        OrderStatus orderStatus = orderCancelledEvent.getOrder().getOrderStatus();
        SagaStatus sagaStatus = orderSagaHelper.orderStatusToSagaStatus(orderStatus);
        updateApprovalOutboxMessage(orderApprovalOutboxMessage, orderStatus, sagaStatus);
        approvalOutboxHelper.save(orderApprovalOutboxMessage);
        paymentOutboxHelper.savePaymentOutboxMessage(orderDataMapper.orderCancelledEventToOrderPaymentEventPayload(orderCancelledEvent),
                                                     orderStatus,
                                                     sagaStatus,
                                                     OutboxStatus.STARTED,
                                                     UUID.fromString(restaurantApprovalResponse.getSagaId()));
        
    }
    
    
    private Order approveOrder(final String orderId) {
        log.info("Approving order with id: {}", orderId);
        Order order = orderSagaHelper.findOrder(orderId);
        orderDomainService.approveOrder(order);
        orderSagaHelper.save(order);
        return order;
    }
    
    private void updateApprovalOutboxMessage(
        OrderApprovalOutboxMessage orderApprovalOutboxMessage,
        final OrderStatus orderStatus,
        final SagaStatus sagaStatus) {
        
        orderApprovalOutboxMessage.setSagaStatus(sagaStatus);
        orderApprovalOutboxMessage.setOrderStatus(orderStatus);
        orderApprovalOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of(UTC)));
        
    }
    
    private OrderPaymentOutboxMessage getUpdatedPaymentOutboxMessage(
        final String sagaId,
        final OrderStatus orderStatus,
        final SagaStatus sagaStatus) {
        Optional<OrderPaymentOutboxMessage> orderPaymentOutboxMessageResponse =
            paymentOutboxHelper.getOrderPaymentOutboxMessageBySagaIdAndSagaStatus(UUID.fromString(sagaId), SagaStatus.PROCESSING);
        
        if (orderPaymentOutboxMessageResponse.isEmpty()) {
            throw new OrderDomainException("Approval outbox message not found in state :" + SagaStatus.PROCESSING.name());
        }
        OrderPaymentOutboxMessage orderPaymentOutboxMessage = orderPaymentOutboxMessageResponse.get();
        orderPaymentOutboxMessage.setSagaStatus(sagaStatus);
        orderPaymentOutboxMessage.setOrderStatus(orderStatus);
        orderPaymentOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of(UTC)));
        return orderPaymentOutboxMessage;
    }
    
    private OrderCancelledEvent rollBackOrder(final RestaurantApprovalResponse restaurantApprovalResponse) {
        String orderId = restaurantApprovalResponse.getOrderId();
        log.info("Cancelling order with id: {}", orderId);
        Order order = orderSagaHelper.findOrder(orderId);
        OrderCancelledEvent orderCancelledEvent = orderDomainService.cancelOrderPayment(order, restaurantApprovalResponse.getFailureMessages());
        orderSagaHelper.save(order);
        return orderCancelledEvent;
    }
    
}
