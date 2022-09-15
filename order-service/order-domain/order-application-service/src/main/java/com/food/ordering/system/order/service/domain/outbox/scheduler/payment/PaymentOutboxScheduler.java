package com.food.ordering.system.order.service.domain.outbox.scheduler.payment;

import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.payment.PaymentRequestMessagePublisher;
import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PaymentOutboxScheduler implements OutboxScheduler {
    
    private final PaymentOutboxHelper paymentOutboxHelper;
    
    private final PaymentRequestMessagePublisher paymentRequestMessagePublisher;
    
    public PaymentOutboxScheduler(
        final PaymentOutboxHelper paymentOutboxHelper,
        final PaymentRequestMessagePublisher paymentRequestMessagePublisher) {
        this.paymentOutboxHelper = paymentOutboxHelper;
        this.paymentRequestMessagePublisher = paymentRequestMessagePublisher;
    }
    
    
    @Override
    @Transactional
    @Scheduled(fixedDelayString = "${order-service.outbox-scheduler-fixed-rate}",
               initialDelayString = "${order-service.outbox-scheduler-initial-delay}")
    public void processOutboxMessage() {
        paymentOutboxHelper.getOrderPaymentOutboxMessageByOutboxStatusAndSagaStatus(OutboxStatus.STARTED, SagaStatus.STARTED, SagaStatus.COMPENSATING)
                           .ifPresent(orderPaymentOutboxMessages -> {
                               log.info("Received {} OrderPaymentOutboxMessage with ids : {}, sending to message bus",
                                        orderPaymentOutboxMessages.size(),
                                        orderPaymentOutboxMessages.stream()
                                                                  .map(orderPaymentOutboxMessage -> orderPaymentOutboxMessage.getId().toString())
                                                                  .collect(Collectors.joining(", ")));
                               orderPaymentOutboxMessages.forEach(
                                   orderPaymentOutboxMessage -> paymentRequestMessagePublisher.publish(orderPaymentOutboxMessage, this::updateOutboxStatus));
                           });
    }
    
    private void updateOutboxStatus(OrderPaymentOutboxMessage orderPaymentOutboxMessage, OutboxStatus outboxStatus) {
        orderPaymentOutboxMessage.setOutboxStatus(outboxStatus);
        paymentOutboxHelper.save(orderPaymentOutboxMessage);
        log.info("OrderPaymentOutboxMessage status is updated with status : {}", outboxStatus);
    }
    
}
