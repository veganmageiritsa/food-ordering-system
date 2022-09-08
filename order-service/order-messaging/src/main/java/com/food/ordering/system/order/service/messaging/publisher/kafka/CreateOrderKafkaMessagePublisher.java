package com.food.ordering.system.order.service.messaging.publisher.kafka;

import org.springframework.stereotype.Component;

import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.producer.KafkaMessageHelper;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.order.service.domain.config.OrderServiceConfigData;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.payment.OrderCreatedPaymentRequestMessagePublisher;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CreateOrderKafkaMessagePublisher implements OrderCreatedPaymentRequestMessagePublisher {
    
    private final OrderMessagingDataMapper orderMessagingDataMapper;
    
    private final OrderServiceConfigData orderServiceConfigData;
    
    private final KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer;
    
    private final KafkaMessageHelper kafkaMessageHelper;
    
    public CreateOrderKafkaMessagePublisher(
        final OrderMessagingDataMapper orderMessagingDataMapper,
        final OrderServiceConfigData orderServiceConfigData,
        final KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer,
        final KafkaMessageHelper kafkaMessageHelper) {
        this.orderMessagingDataMapper = orderMessagingDataMapper;
        this.orderServiceConfigData = orderServiceConfigData;
        this.kafkaProducer = kafkaProducer;
        this.kafkaMessageHelper = kafkaMessageHelper;
    }
    
    @Override
    public void publish(final OrderCreatedEvent domainEvent) {
        final var orderId = domainEvent.getOrder().getId().getValue().toString();
        log.info("Received OrderCreatedEvent with id : {}", orderId);
        try {
            final var paymentRequestAvroModel = orderMessagingDataMapper.orderCreatedEventToPaymentRequestAvroModel(domainEvent);
            final var paymentRequestTopicName = orderServiceConfigData.getPaymentRequestTopicName();
            kafkaProducer.send(
                paymentRequestTopicName,
                orderId,
                paymentRequestAvroModel,
                kafkaMessageHelper.getKafkaCallback(paymentRequestTopicName, paymentRequestAvroModel, orderId, "PaymentRequestAvroModel"));
            log.info("PaymentRequestAvroModel send to kafka  for order  id : {}", orderId);
        } catch (Exception e) {
            log.error("Error While sending PaymentRequestAvroModel message to kafka with order ID: {} error: {}",
                      orderId, e.getMessage());
        }
        
    }
    
    
}
