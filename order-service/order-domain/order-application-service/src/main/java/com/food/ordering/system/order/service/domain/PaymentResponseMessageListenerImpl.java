package com.food.ordering.system.order.service.domain;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.food.ordering.system.order.service.domain.dto.message.PaymentResponse;
import static com.food.ordering.system.order.service.domain.entity.Order.FAILURE_MESSAGE_DELIMITER;
import com.food.ordering.system.order.service.domain.ports.input.message.listener.payment.PaymentResponseMessageListener;
import lombok.extern.slf4j.Slf4j;

@Validated
@Slf4j
@Service
public class PaymentResponseMessageListenerImpl implements PaymentResponseMessageListener {
    
    private final OrderPaymentSaga orderPaymentSaga;
    
    public PaymentResponseMessageListenerImpl(final OrderPaymentSaga orderPaymentSaga) {
        this.orderPaymentSaga = orderPaymentSaga;
    }
    
    @Override
    public void paymentCompleted(final PaymentResponse paymentResponse) {
        orderPaymentSaga.process(paymentResponse);
        log.info("Order Payment Saga process operation is completed for order with id: {}", paymentResponse.getOrderId());
    }
    
    @Override
    public void paymentCanceled(final PaymentResponse paymentResponse) {
        orderPaymentSaga.rollback(paymentResponse);
        log.info("order with id: {} is roll backed with failure messages : {}",
                 paymentResponse.getOrderId(),
                 String.join(FAILURE_MESSAGE_DELIMITER, paymentResponse.getFailureMessages()));
        
    }
    
}
