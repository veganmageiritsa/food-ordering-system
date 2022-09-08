package com.food.ordering.system.payment.service.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.food.ordering.system.payment.service.domain.entity.CreditEntry;
import com.food.ordering.system.payment.service.domain.entity.CreditHistory;
import com.food.ordering.system.payment.service.domain.entity.Payment;
import com.food.ordering.system.payment.service.domain.event.PaymentEvent;
import com.food.ordering.system.payment.service.domain.exception.PaymentApplicationServiceException;
import com.food.ordering.system.payment.service.domain.mapper.PaymentDataMapper;
import com.food.ordering.system.payment.service.domain.ports.output.message.listener.PaymentCancelledMessagePublisher;
import com.food.ordering.system.payment.service.domain.ports.output.message.listener.PaymentCompletedMessagePublisher;
import com.food.ordering.system.payment.service.domain.ports.output.message.listener.PaymentFailedMessagePublisher;
import com.food.ordering.system.payment.service.domain.ports.output.repository.CreditEntryRepository;
import com.food.ordering.system.payment.service.domain.ports.output.repository.CreditHistoryRepository;
import com.food.ordering.system.payment.service.domain.ports.output.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PaymentRequestHelper {
    
    private final PaymentDomainService paymentDomainService;
    
    private final PaymentDataMapper paymentDataMapper;
    
    private final PaymentRepository paymentRepository;
    
    private final CreditEntryRepository creditEntryRepository;
    
    private final CreditHistoryRepository creditHistoryRepository;
    
    private final PaymentCompletedMessagePublisher paymentCompletedEventDomainEventPublisher;
    
    private final PaymentCancelledMessagePublisher paymentCancelledEventDomainEventPublisher;
    
    private final PaymentFailedMessagePublisher paymentFailedEventDomainEventPublisher;
    
    public PaymentRequestHelper(
        final PaymentDomainService paymentDomainService,
        final PaymentDataMapper paymentDataMapper,
        final PaymentRepository paymentRepository,
        final CreditEntryRepository creditEntryRepository,
        final CreditHistoryRepository creditHistoryRepository,
        final PaymentCompletedMessagePublisher paymentCompletedMessagePublisher,
        final PaymentCancelledMessagePublisher paymentCancelledEventDomainEventPublisher,
        final PaymentFailedMessagePublisher paymentFailedEventDomainEventPublisher) {
        this.paymentDomainService = paymentDomainService;
        this.paymentDataMapper = paymentDataMapper;
        this.paymentRepository = paymentRepository;
        this.creditEntryRepository = creditEntryRepository;
        this.creditHistoryRepository = creditHistoryRepository;
        this.paymentCompletedEventDomainEventPublisher = paymentCompletedMessagePublisher;
        this.paymentCancelledEventDomainEventPublisher = paymentCancelledEventDomainEventPublisher;
        this.paymentFailedEventDomainEventPublisher = paymentFailedEventDomainEventPublisher;
    }
    
    @Transactional
    public PaymentEvent persistPayment(PaymentRequest paymentRequest) {
        log.info("Received Payment Event for order id : {}", paymentRequest.getOrderId());
        Payment payment = paymentDataMapper.paymentRequestToPayment(paymentRequest);
        CreditEntry creditEntry = getCreditEntry(payment.getCustomerId());
        List<CreditHistory> creditHistories = getCreditHistory(payment.getCustomerId());
        List<String> failureMessages = new ArrayList<>();
        final var paymentEvent = paymentDomainService.validateAndInitiatePayment(payment, creditEntry, creditHistories, failureMessages,
                                                                                 paymentCompletedEventDomainEventPublisher,
                                                                                 paymentFailedEventDomainEventPublisher);
        persistPaymentDbObjects(payment, creditEntry, creditHistories, failureMessages);
        return paymentEvent;
    }
    
    @Transactional
    public PaymentEvent persistCancelPayment(PaymentRequest paymentRequest) {
        Optional<Payment> paymentResponse = paymentRepository.findByOrderId(UUID.fromString(paymentRequest.getOrderId()));
        if (paymentResponse.isEmpty()) {
            log.error("Payment with order id : {} could not be found", paymentRequest.getOrderId());
            throw new PaymentApplicationServiceException("Payment with order id : " + paymentRequest.getOrderId() + " could not be found");
        }
        Payment payment = paymentResponse.get();
        CreditEntry creditEntry = getCreditEntry(payment.getCustomerId());
        List<CreditHistory> creditHistories = getCreditHistory(payment.getCustomerId());
        List<String> failureMessages = new ArrayList<>();
        final var paymentEvent = paymentDomainService.validateAndCancelPayment(payment, creditEntry, creditHistories, failureMessages,
                                                                               paymentCancelledEventDomainEventPublisher,
                                                                               paymentFailedEventDomainEventPublisher);
        persistPaymentDbObjects(payment, creditEntry, creditHistories, failureMessages);
        return paymentEvent;
    }
    
    private void persistPaymentDbObjects(
        final Payment payment,
        final CreditEntry creditEntry,
        final List<CreditHistory> creditHistories,
        final List<String> failureMessages) {
        paymentRepository.save(payment);
        if (failureMessages.isEmpty()) {
            creditEntryRepository.save(creditEntry);
            creditHistoryRepository.save(creditHistories.get(creditHistories.size() - 1));
            
        }
    }
    
    
    private CreditEntry getCreditEntry(final CustomerId customerId) {
        return creditEntryRepository
            .findByCustomerId(customerId)
            .orElseThrow(
                () -> new PaymentApplicationServiceException("Could not find credit entry for customer : " + customerId.getValue()));
        
    }
    
    private List<CreditHistory> getCreditHistory(final CustomerId customerId) {
        return creditHistoryRepository
            .findByCustomerId(customerId)
            .orElseThrow(
                () -> new PaymentApplicationServiceException("Could not find credit history for customer : " + customerId.getValue()));
    }
    
}
