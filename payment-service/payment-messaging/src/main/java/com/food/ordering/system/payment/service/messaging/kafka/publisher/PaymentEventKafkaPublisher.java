package com.food.ordering.system.payment.service.messaging.kafka.publisher;

import java.sql.SQLException;
import java.util.function.BiConsumer;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import com.food.ordering.system.kafka.order.avro.model.PaymentResponseAvroModel;
import com.food.ordering.system.kafka.producer.KafkaMessageHelper;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.payment.service.domain.config.PaymentServiceConfigData;
import com.food.ordering.system.payment.service.domain.exception.PaymentApplicationServiceException;
import com.food.ordering.system.payment.service.domain.exception.PaymentNotFoundException;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderEventPayload;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.payment.service.domain.ports.output.message.publisher.PaymentResponseMessagePublisher;
import com.food.ordering.system.payment.service.messaging.mapper.PaymentMessagingDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PSQLState;

@Slf4j
@Component
public class PaymentEventKafkaPublisher implements PaymentResponseMessagePublisher {
    
    private final PaymentMessagingDataMapper paymentMessagingDataMapper;
    
    private final KafkaProducer<String, PaymentResponseAvroModel> kafkaProducer;
    
    private final PaymentServiceConfigData paymentServiceConfigData;
    
    private final KafkaMessageHelper kafkaMessageHelper;
    
    public PaymentEventKafkaPublisher(
        final PaymentMessagingDataMapper paymentMessagingDataMapper,
        final KafkaProducer<String, PaymentResponseAvroModel> kafkaProducer,
        final PaymentServiceConfigData paymentServiceConfigData, final KafkaMessageHelper kafkaMessageHelper) {
        this.paymentMessagingDataMapper = paymentMessagingDataMapper;
        this.kafkaProducer = kafkaProducer;
        this.paymentServiceConfigData = paymentServiceConfigData;
        this.kafkaMessageHelper = kafkaMessageHelper;
    }
    
    @Override
    public void publish(
        final OrderOutboxMessage orderOutboxMessage,
        final BiConsumer<OrderOutboxMessage, OutboxStatus> outboxCallback) {
        
        OrderEventPayload orderEventPayload = kafkaMessageHelper.getOrderEventPayload(orderOutboxMessage.getPayload(), OrderEventPayload.class);
        String sagaId = orderOutboxMessage.getSagaId().toString();
        
        PaymentResponseAvroModel paymentResponseAvroModel = paymentMessagingDataMapper.orderEventPayloadToPaymentResponseAvroModel(sagaId, orderEventPayload);
        try {
            final var paymentResponseTopicName = paymentServiceConfigData.getPaymentResponseTopicName();
            kafkaProducer.send(paymentResponseTopicName,
                               sagaId,
                               paymentResponseAvroModel,
                               kafkaMessageHelper.getKafkaCallback(paymentResponseTopicName,
                                                                   paymentResponseAvroModel,
                                                                   orderOutboxMessage,
                                                                   outboxCallback,
                                                                   orderEventPayload.getOrderId(),
                                                                   "PaymentResponseAvroModel"));
            
            log.info("RestaurantApprovalAvroModel sent to kafka for order id: {} and saga id: {}", paymentResponseAvroModel.getOrderId(), sagaId);
        } catch (DataAccessException e) {
            SQLException sqlException = (SQLException) e.getRootCause();
            if (sqlException != null && sqlException.getSQLState() != null
                && PSQLState.UNIQUE_VIOLATION.getState().equals(sqlException.getSQLState())) {
                log.error("Caught unique violation exception with sql state : {} in PaymentRequestKafkaListener order id: {}",
                          sqlException.getSQLState(),
                          paymentResponseAvroModel.getOrderId());
                
            }
            else {
                throw new PaymentApplicationServiceException(
                    "Throwing Data Access Exception from PaymentRequestKafkaListener: " + e.getMessage(), e
                );
            }
        } catch (PaymentNotFoundException e) {
            log.error("No payment found for order id: {}", paymentResponseAvroModel.getOrderId());
        }
        
    }
    
}
