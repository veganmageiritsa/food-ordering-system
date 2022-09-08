package com.food.ordering.system.payment.service.domain;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static com.food.ordering.system.domain.DomainConstants.UTC;
import com.food.ordering.system.domain.event.publisher.DomainEventPublisher;
import com.food.ordering.system.domain.valueobject.Money;
import com.food.ordering.system.domain.valueobject.PaymentStatus;
import com.food.ordering.system.payment.service.domain.entity.CreditEntry;
import com.food.ordering.system.payment.service.domain.entity.CreditHistory;
import com.food.ordering.system.payment.service.domain.entity.Payment;
import com.food.ordering.system.payment.service.domain.event.PaymentCancelledEvent;
import com.food.ordering.system.payment.service.domain.event.PaymentCompletedEvent;
import com.food.ordering.system.payment.service.domain.event.PaymentEvent;
import com.food.ordering.system.payment.service.domain.event.PaymentFailedEvent;
import com.food.ordering.system.payment.service.domain.valueobject.CreditHistoryId;
import com.food.ordering.system.payment.service.domain.valueobject.TransactionType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PaymentDomainServiceImpl implements PaymentDomainService {
    
    @Override
    public PaymentEvent validateAndInitiatePayment(
        Payment payment,
        CreditEntry creditEntry,
        List<CreditHistory> creditHistories,
        List<String> failureMessages,
        final DomainEventPublisher<PaymentCompletedEvent> paymentCompletedEventDomainEventPublisher,
        final DomainEventPublisher<PaymentFailedEvent> paymentFailedEventDomainEventPublisher) {
        
        payment.validatePayment(failureMessages);
        payment.initializePayment();
        validateCreditEntry(payment, creditEntry, failureMessages);
        subtractCreditEntry(payment, creditEntry);
        updateCreditHistory(payment, creditHistories, TransactionType.DEBIT);
        validateCreditHistory(creditEntry, creditHistories, failureMessages);
        if (failureMessages.isEmpty()) {
            log.info("Payment is initiated for order id : {}", payment.getOrderId().getValue());
            payment.updateStatus(PaymentStatus.COMPLETED);
            return new PaymentCompletedEvent(payment, ZonedDateTime.now(ZoneId.of(UTC)), paymentCompletedEventDomainEventPublisher);
        }
        else {
            log.info("Payment initiation has failed for order id : {}", payment.getOrderId().getValue());
            payment.updateStatus(PaymentStatus.FAILED);
            return new PaymentFailedEvent(payment, ZonedDateTime.now(ZoneId.of(UTC)), failureMessages, paymentFailedEventDomainEventPublisher);
        }
    }
    
    @Override
    public PaymentEvent validateAndCancelPayment(
        final Payment payment,
        final CreditEntry creditEntry,
        final List<CreditHistory> creditHistories,
        final List<String> failureMessages,
        final DomainEventPublisher<PaymentCancelledEvent> paymentCancelledEventDomainEventPublisher,
        final DomainEventPublisher<PaymentFailedEvent> paymentFailedEventDomainEventPublisher) {
        payment.validatePayment(failureMessages);
        addCreditEntry(payment, creditEntry);
        updateCreditHistory(payment, creditHistories, TransactionType.CREDIT);
        if (failureMessages.isEmpty()) {
            log.info("Payment is cancelled for order id : {}", payment.getOrderId().getValue());
            payment.updateStatus(PaymentStatus.CANCELLED);
            return new PaymentCancelledEvent(payment, ZonedDateTime.now(ZoneId.of(UTC)), paymentCancelledEventDomainEventPublisher);
        }
        else {
            log.info("Payment cancellation has failed for order id : {}", payment.getOrderId().getValue());
            payment.updateStatus(PaymentStatus.FAILED);
            return new PaymentFailedEvent(payment, ZonedDateTime.now(ZoneId.of(UTC)), failureMessages, paymentFailedEventDomainEventPublisher);
        }
    }
    
    
    private void validateCreditHistory(
        final CreditEntry creditEntry,
        final List<CreditHistory> creditHistories,
        final List<String> failureMessages) {
        
        final var totalCreditHistory = getTotalCreditHistory(creditHistories, TransactionType.CREDIT);
        
        final var totalDebitHistory = getTotalCreditHistory(creditHistories, TransactionType.DEBIT);
        
        if (totalDebitHistory.isGreaterThan(totalCreditHistory)) {
            log.error("Customer with Id: {} doesn't have enough credit according to credit history", creditEntry.getCustomerId().getValue());
            failureMessages.add("Customer with id : " + creditEntry.getCustomerId().getValue() + " doesn't have enough credit for payment");
        }
        if (!creditEntry.getTotalCreditAmount().equals(totalCreditHistory.subtract(totalDebitHistory))) {
            log.error("Credit history total is not equal to current credit for Customer with Id: {}", creditEntry.getCustomerId().getValue());
            failureMessages.add("Credit history total is not equal to current credit for Customer with Id: " + creditEntry.getCustomerId().getValue());
        }
        
    }
    
    private Money getTotalCreditHistory(final List<CreditHistory> creditHistories, final TransactionType credit) {
        return creditHistories
            .stream()
            .filter(creditHistory -> creditHistory.getTransactionType().equals(credit))
            .map(CreditHistory::getAmount)
            .reduce(Money.ZERO, Money::add);
    }
    
    private void updateCreditHistory(
        final Payment payment,
        List<CreditHistory> creditHistories,
        final TransactionType transactionType) {
        creditHistories.add(CreditHistory.builder()
                                         .creditHistoryId(new CreditHistoryId(UUID.randomUUID()))
                                         .transactionType(transactionType)
                                         .amount(payment.getPrice())
                                         .customerId(payment.getCustomerId())
                                         .build());
    }
    
    private void subtractCreditEntry(final Payment payment, CreditEntry creditEntry) {
        creditEntry.subtractCreditAmount(payment.getPrice());
    }
    
    private void validateCreditEntry(final Payment payment, final CreditEntry creditEntry, final List<String> failureMessages) {
        if (payment.getPrice().isGreaterThan(creditEntry.getTotalCreditAmount())) {
            log.error("Customer with id : {} doesn't have enough credit for payment", payment.getCustomerId().getValue());
            failureMessages.add("Customer with id : " + payment.getCustomerId().getValue() + " doesn't have enough credit for payment");
        }
    }
    
    private void addCreditEntry(final Payment payment, final CreditEntry creditEntry) {
        creditEntry.addCreditAmount(payment.getPrice());
    }
    
}
