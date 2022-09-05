package com.food.ordering.system.order.service.domain;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.food.ordering.system.order.service.domain.dto.message.PaymentResponse;
import com.food.ordering.system.order.service.domain.ports.input.message.listener.payment.PaymentResponseMessageListener;
import lombok.extern.slf4j.Slf4j;

@Validated
@Slf4j
@Service
public class PaymentResponseMessageListenerImpl implements PaymentResponseMessageListener {
    
    @Override
    public void paymentCompleted(final PaymentResponse paymentResponse) {
    
    }
    
    @Override
    public void paymentCanceled(final PaymentResponse paymentResponse) {
    
    }
    
}
