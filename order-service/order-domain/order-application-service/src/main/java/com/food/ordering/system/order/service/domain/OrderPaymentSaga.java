package com.food.ordering.system.order.service.domain;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.food.ordering.system.domain.DomainConstants.UTC;
import com.food.ordering.system.domain.valueobject.OrderStatus;
import com.food.ordering.system.domain.valueobject.PaymentStatus;
import com.food.ordering.system.order.service.domain.dto.message.PaymentResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
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
public class OrderPaymentSaga implements SagaStep<PaymentResponse> {
    
    private final OrderDomainService orderDomainService;
    
    private final OrderSagaHelper orderSagaHelper;
    
    private final PaymentOutboxHelper paymentOutboxHelper;
    
    private final ApprovalOutboxHelper approvalOutboxHelper;
    
    private final OrderDataMapper orderDataMapper;
    
    public OrderPaymentSaga(
        final OrderDomainService orderDomainService,
        final OrderSagaHelper orderSagaHelper,
        final PaymentOutboxHelper paymentOutboxHelper,
        final ApprovalOutboxHelper approvalOutboxHelper,
        final OrderDataMapper orderDataMapper) {
        this.orderDomainService = orderDomainService;
        this.orderSagaHelper = orderSagaHelper;
        this.paymentOutboxHelper = paymentOutboxHelper;
        this.approvalOutboxHelper = approvalOutboxHelper;
        this.orderDataMapper = orderDataMapper;
    }
    
    @Override
    public void process(final PaymentResponse paymentResponse) {
        Optional<OrderPaymentOutboxMessage> orderPaymentOutboxMessageResponse =
            paymentOutboxHelper.getOrderPaymentOutboxMessageBySagaIdAndSagaStatus(UUID.fromString(paymentResponse.getSagaId()), SagaStatus.STARTED);
        if (orderPaymentOutboxMessageResponse.isEmpty()) {
            log.info("An outbox message with saga id: {} is already processed", paymentResponse.getSagaId());
            return;
        }
        OrderPaymentOutboxMessage orderPaymentOutboxMessage = orderPaymentOutboxMessageResponse.get();
        
        String orderId = paymentResponse.getOrderId();
        
        OrderPaidEvent orderPaidEvent = completePaymentForOrder(orderId);
        
        SagaStatus sagaStatus = orderSagaHelper.orderStatusToSagaStatus(orderPaidEvent.getOrder().getOrderStatus());
        
        updateOrderPaymentOutboxMessage(orderPaymentOutboxMessage, orderPaidEvent.getOrder().getOrderStatus(), sagaStatus);
        
        paymentOutboxHelper.save(orderPaymentOutboxMessage);
        approvalOutboxHelper.saveApprovalOutboxMessage(orderDataMapper.orderPaidEventToOrderApprovalEventPayload(orderPaidEvent),
                                                       orderPaidEvent.getOrder().getOrderStatus(),
                                                       sagaStatus,
                                                       OutboxStatus.STARTED,
                                                       UUID.fromString(paymentResponse.getSagaId()));
    }
    
    private OrderPaidEvent completePaymentForOrder(final String orderId) {
        log.info("Completing payment for order with id: {}", orderId);
        Order order = orderSagaHelper.findOrder(orderId);
        OrderPaidEvent orderPaidEvent = orderDomainService.payOrder(order);
        orderSagaHelper.save(order);
        return orderPaidEvent;
    }
    
    private void updateOrderPaymentOutboxMessage(
        final OrderPaymentOutboxMessage orderPaymentOutboxMessage,
        final OrderStatus orderStatus,
        final SagaStatus sagaStatus) {
        orderPaymentOutboxMessage.setSagaStatus(sagaStatus);
        orderPaymentOutboxMessage.setOrderStatus(orderStatus);
        orderPaymentOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of(UTC)));
    }
    
    
    @Override
    public void rollback(final PaymentResponse paymentResponse) {
        Optional<OrderPaymentOutboxMessage> orderPaymentOutboxMessageResponse =
            paymentOutboxHelper.getOrderPaymentOutboxMessageBySagaIdAndSagaStatus(UUID.fromString(paymentResponse.getSagaId()),
                                                                                  getCurrentSagaStatus(paymentResponse.getPaymentStatus()));
        if (orderPaymentOutboxMessageResponse.isEmpty()) {
            log.info("An outbox message with saga id: {} is already roll backed", paymentResponse.getSagaId());
            return;
        }
        OrderPaymentOutboxMessage orderPaymentOutboxMessage = orderPaymentOutboxMessageResponse.get();
        
        Order order = rollbackPaymentForOrder(paymentResponse);
        
        SagaStatus sagaStatus = orderSagaHelper.orderStatusToSagaStatus(order.getOrderStatus());
        
        updateOrderPaymentOutboxMessage(orderPaymentOutboxMessage, order.getOrderStatus(), sagaStatus);
        
        paymentOutboxHelper.save(orderPaymentOutboxMessage);
        
        if (PaymentStatus.CANCELLED.equals(paymentResponse.getPaymentStatus())) {
            OrderApprovalOutboxMessage orderApprovalOutboxMessage = updateApprovalOutboxMessage(paymentResponse.getSagaId(), order.getOrderStatus(),
                                                                                                sagaStatus);
            
            approvalOutboxHelper.save(orderApprovalOutboxMessage);
        }
        
        log.info("order with id: {} is cancelled ", order.getId().getValue());
    }
    
    
    private SagaStatus[] getCurrentSagaStatus(PaymentStatus paymentStatus) {
        switch (paymentStatus) {
            case COMPLETED:
                return new SagaStatus[]{ SagaStatus.STARTED };
            case CANCELLED:
                return new SagaStatus[]{ SagaStatus.PROCESSING };
            case FAILED:
                return new SagaStatus[]{ SagaStatus.STARTED, SagaStatus.PROCESSING };
        }
        return new SagaStatus[0];
    }
    
    private Order rollbackPaymentForOrder(PaymentResponse paymentResponse) {
        String orderId = paymentResponse.getOrderId();
        log.info("Cancelling payment for order with id: {}", orderId);
        log.info("Cancelling order with id: {}", paymentResponse.getOrderId());
        Order order = orderSagaHelper.findOrder(orderId);
        orderDomainService.cancelOrder(order, paymentResponse.getFailureMessages());
        orderSagaHelper.save(order);
        return order;
    }
    
    private OrderApprovalOutboxMessage updateApprovalOutboxMessage(final String sagaId, final OrderStatus orderStatus, final SagaStatus sagaStatus) {
        Optional<OrderApprovalOutboxMessage> orderApprovalOutboxMessageResponse = approvalOutboxHelper.getApprovalOutboxMessageBySagaIdAndSagaStatus(
            UUID.fromString(sagaId), SagaStatus.COMPENSATING);
        
        if (orderApprovalOutboxMessageResponse.isEmpty()) {
            throw new OrderDomainException("Approval outbox message not found in state :" + SagaStatus.COMPENSATING.name());
        }
        OrderApprovalOutboxMessage orderApprovalOutboxMessage = orderApprovalOutboxMessageResponse.get();
        orderApprovalOutboxMessage.setSagaStatus(sagaStatus);
        orderApprovalOutboxMessage.setOrderStatus(orderStatus);
        orderApprovalOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of(UTC)));
        return orderApprovalOutboxMessage;
    }
    
}
