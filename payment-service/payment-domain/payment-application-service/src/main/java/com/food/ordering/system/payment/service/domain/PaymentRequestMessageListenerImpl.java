package com.food.ordering.system.payment.service.domain;

import org.springframework.stereotype.Service;

import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.food.ordering.system.payment.service.domain.event.PaymentEvent;
import com.food.ordering.system.payment.service.domain.ports.input.message.listener.PaymentRequestMessageListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PaymentRequestMessageListenerImpl implements PaymentRequestMessageListener {
    
    private final PaymentRequestHelper paymentRequestHelper;
    
    public PaymentRequestMessageListenerImpl(
        final PaymentRequestHelper paymentRequestHelper) {
        this.paymentRequestHelper = paymentRequestHelper;
    }
    
    @Override
    public void completePayment(final PaymentRequest paymentRequest) {
        PaymentEvent paymentEvent = paymentRequestHelper.persistPayment(paymentRequest);
        publishEvent(paymentEvent);
    }
    
    @Override
    public void cancelPayment(final PaymentRequest paymentRequest) {
        PaymentEvent paymentEvent = paymentRequestHelper.persistCancelPayment(paymentRequest);
        publishEvent(paymentEvent);
    }
    
    private void publishEvent(final PaymentEvent paymentEvent) {
        log.info("Publishing Payment Event for payment id : {} and order Id : {} ",
                 paymentEvent.getPayment().getId(),
                 paymentEvent.getPayment().getOrderId());
        
        paymentEvent.fire();
    }
    
}
