package com.food.ordering.system.payment.service.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.domain.valueobject.PaymentStatus;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.food.ordering.system.payment.service.domain.entity.CreditEntry;
import com.food.ordering.system.payment.service.domain.entity.CreditHistory;
import com.food.ordering.system.payment.service.domain.entity.Payment;
import com.food.ordering.system.payment.service.domain.exception.PaymentApplicationServiceException;
import com.food.ordering.system.payment.service.domain.exception.PaymentNotFoundException;
import com.food.ordering.system.payment.service.domain.mapper.PaymentDataMapper;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.payment.service.domain.outbox.scheduler.OrderOutboxHelper;
import com.food.ordering.system.payment.service.domain.ports.output.message.publisher.PaymentResponseMessagePublisher;
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
    
    private final OrderOutboxHelper orderOutboxHelper;
    
    private final PaymentResponseMessagePublisher paymentResponseMessagePublisher;
    
    public PaymentRequestHelper(
        final PaymentDomainService paymentDomainService,
        final PaymentDataMapper paymentDataMapper,
        final PaymentRepository paymentRepository,
        final CreditEntryRepository creditEntryRepository,
        final CreditHistoryRepository creditHistoryRepository,
        final OrderOutboxHelper orderOutboxHelper,
        final PaymentResponseMessagePublisher paymentResponseMessagePublisher) {
        this.paymentDomainService = paymentDomainService;
        this.paymentDataMapper = paymentDataMapper;
        this.paymentRepository = paymentRepository;
        this.creditEntryRepository = creditEntryRepository;
        this.creditHistoryRepository = creditHistoryRepository;
        this.orderOutboxHelper = orderOutboxHelper;
        this.paymentResponseMessagePublisher = paymentResponseMessagePublisher;
    }
    
    @Transactional
    public void persistPayment(PaymentRequest paymentRequest) {
        if (publishIfOutboxMessageProcessedForPayment(paymentRequest, PaymentStatus.COMPLETED)) {
            log.info("An outbox message with saga id : {} is already saved to database", paymentRequest.getSagaId());
            return;
        }
        log.info("Received Payment Event for order id : {}", paymentRequest.getOrderId());
        Payment payment = paymentDataMapper.paymentRequestToPayment(paymentRequest);
        CreditEntry creditEntry = getCreditEntry(payment.getCustomerId());
        List<CreditHistory> creditHistories = getCreditHistory(payment.getCustomerId());
        List<String> failureMessages = new ArrayList<>();
        final var paymentEvent = paymentDomainService.validateAndInitiatePayment(payment, creditEntry, creditHistories, failureMessages);
        persistPaymentDbObjects(payment, creditEntry, creditHistories, failureMessages);
        orderOutboxHelper.saveOrderOutboxMessage(paymentDataMapper.paymentEventToOrderEventPayload(paymentEvent),
                                                 paymentEvent.getPayment().getPaymentStatus(),
                                                 OutboxStatus.STARTED,
                                                 UUID.fromString(paymentRequest.getSagaId()));
    }
    
    @Transactional
    public void persistCancelPayment(PaymentRequest paymentRequest) {
        if (publishIfOutboxMessageProcessedForPayment(paymentRequest, PaymentStatus.CANCELLED)) {
            log.info("An outbox message with saga id : {} is already saved to database", paymentRequest.getSagaId());
            return;
        }
        Optional<Payment> paymentResponse = paymentRepository.findByOrderId(UUID.fromString(paymentRequest.getOrderId()));
        if (paymentResponse.isEmpty()) {
            log.error("Payment with order id : {} could not be found", paymentRequest.getOrderId());
            throw new PaymentNotFoundException("Payment with order id : " + paymentRequest.getOrderId() + " could not be found");
        }
        Payment payment = paymentResponse.get();
        CreditEntry creditEntry = getCreditEntry(payment.getCustomerId());
        List<CreditHistory> creditHistories = getCreditHistory(payment.getCustomerId());
        List<String> failureMessages = new ArrayList<>();
        final var paymentEvent = paymentDomainService.validateAndCancelPayment(payment, creditEntry, creditHistories, failureMessages);
        persistPaymentDbObjects(payment, creditEntry, creditHistories, failureMessages);
        
        orderOutboxHelper.saveOrderOutboxMessage(paymentDataMapper.paymentEventToOrderEventPayload(paymentEvent),
                                                 paymentEvent.getPayment().getPaymentStatus(),
                                                 OutboxStatus.STARTED,
                                                 UUID.fromString(paymentRequest.getSagaId()));
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
    
    private boolean publishIfOutboxMessageProcessedForPayment(
        PaymentRequest paymentRequest,
        PaymentStatus paymentStatus) {
        Optional<OrderOutboxMessage> orderOutboxMessageResponse =
            orderOutboxHelper.getCompletedOrderOutboxMessageBySagaIdAndPaymentStatus(UUID.fromString(paymentRequest.getSagaId()),
                                                                                     paymentStatus);
        
        if (orderOutboxMessageResponse.isPresent()) {
            paymentResponseMessagePublisher.publish(orderOutboxMessageResponse.get(), orderOutboxHelper::updateOutboxMessage);
            return true;
        }
        return false;
    }
    
}
