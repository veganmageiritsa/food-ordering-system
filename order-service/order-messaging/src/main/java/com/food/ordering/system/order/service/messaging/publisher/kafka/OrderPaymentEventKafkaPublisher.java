package com.food.ordering.system.order.service.messaging.publisher.kafka;

import java.util.function.BiConsumer;

import org.springframework.stereotype.Component;

import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.producer.KafkaMessageHelper;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.order.service.domain.config.OrderServiceConfigData;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentEventPayload;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.payment.PaymentRequestMessagePublisher;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import com.food.ordering.system.outbox.OutboxStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrderPaymentEventKafkaPublisher implements PaymentRequestMessagePublisher {
    
    private final OrderMessagingDataMapper orderMessagingDataMapper;
    
    private final KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer;
    
    private final KafkaMessageHelper kafkaMessageHelper;
    
    private final OrderServiceConfigData orderServiceConfigData;
    
    public OrderPaymentEventKafkaPublisher(
        final OrderMessagingDataMapper orderMessagingDataMapper,
        final KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer,
        final KafkaMessageHelper kafkaMessageHelper,
        final OrderServiceConfigData orderServiceConfigData) {
        this.orderMessagingDataMapper = orderMessagingDataMapper;
        this.kafkaProducer = kafkaProducer;
        this.kafkaMessageHelper = kafkaMessageHelper;
        this.orderServiceConfigData = orderServiceConfigData;
    }
    
    @Override
    public void publish(
        final OrderPaymentOutboxMessage orderPaymentOutboxMessage,
        final BiConsumer<OrderPaymentOutboxMessage, OutboxStatus> outboxCallback) {
        OrderPaymentEventPayload orderPaymentEventPayload =
            kafkaMessageHelper.getOrderEventPayload(orderPaymentOutboxMessage.getPayload(), OrderPaymentEventPayload.class);
        String sagaId = orderPaymentOutboxMessage.getSagaId().toString();
        log.info("Received OrderPaymentOutboxMessage for order id : {} and saga id : {} ", orderPaymentEventPayload.getOrderId(), sagaId);
        try {
            PaymentRequestAvroModel paymentRequestAvroModel = orderMessagingDataMapper.orderPaymentEventToPaymentRequestAvroModel(sagaId,
                                                                                                                                  orderPaymentEventPayload);
            String paymentRequestTopicName = orderServiceConfigData.getPaymentRequestTopicName();
            kafkaProducer.send(paymentRequestTopicName,
                               sagaId,
                               paymentRequestAvroModel,
                               kafkaMessageHelper.getKafkaCallback(paymentRequestTopicName,
                                                                   paymentRequestAvroModel,
                                                                   orderPaymentOutboxMessage,
                                                                   outboxCallback,
                                                                   orderPaymentEventPayload.getOrderId(),
                                                                   "PaymentRequestAvroModel"));
            
            log.info("OrderPaymentEventPayload send to Kafka for order id : {} and saga id : {} ", orderPaymentEventPayload.getOrderId(), sagaId);
        } catch (Exception e) {
            log.error("Error while sending OrderPaymentEventPayload to kafka with order id : {} and saga id : {} , error : {} ",
                      orderPaymentEventPayload.getOrderId(), sagaId, e);
            
        }
        
    }
    
    
}
