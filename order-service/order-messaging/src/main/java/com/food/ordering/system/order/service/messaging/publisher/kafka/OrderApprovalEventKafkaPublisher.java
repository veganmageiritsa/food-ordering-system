package com.food.ordering.system.order.service.messaging.publisher.kafka;

import java.util.function.BiConsumer;

import org.springframework.stereotype.Component;

import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.food.ordering.system.kafka.producer.KafkaMessageHelper;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.order.service.domain.config.OrderServiceConfigData;
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalEventPayload;
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.restaurantapproval.RestaurantApprovalRequestMessagePublisher;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import com.food.ordering.system.outbox.OutboxStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrderApprovalEventKafkaPublisher implements RestaurantApprovalRequestMessagePublisher {
    
    private final OrderMessagingDataMapper orderMessagingDataMapper;
    
    private final KafkaProducer<String, RestaurantApprovalRequestAvroModel> kafkaProducer;
    
    private final KafkaMessageHelper kafkaMessageHelper;
    
    private final OrderServiceConfigData orderServiceConfigData;
    
    public OrderApprovalEventKafkaPublisher(
        final OrderMessagingDataMapper orderMessagingDataMapper,
        final KafkaProducer<String, RestaurantApprovalRequestAvroModel> kafkaProducer, final KafkaMessageHelper kafkaMessageHelper,
        final OrderServiceConfigData orderServiceConfigData) {
        this.orderMessagingDataMapper = orderMessagingDataMapper;
        this.kafkaProducer = kafkaProducer;
        this.kafkaMessageHelper = kafkaMessageHelper;
        this.orderServiceConfigData = orderServiceConfigData;
    }
    
    @Override
    public void publish(
        final OrderApprovalOutboxMessage orderApprovalOutboxMessage,
        final BiConsumer<OrderApprovalOutboxMessage, OutboxStatus> outboxCallback) {
        OrderApprovalEventPayload orderApprovalEventPayload =
            kafkaMessageHelper.getOrderEventPayload(orderApprovalOutboxMessage.getPayload(),
                                                    OrderApprovalEventPayload.class);
        
        String sagaId = orderApprovalOutboxMessage.getSagaId().toString();
        log.info("Received OrderApprovalOutboxMessage for order id : {} and saga id : {} ", orderApprovalEventPayload.getOrderId(), sagaId);
        try {
            RestaurantApprovalRequestAvroModel restaurantApprovalRequestAvroModel =
                orderMessagingDataMapper.orderApprovalEventToRestaurantApprovalRequestAvroModel(sagaId,
                                                                                                orderApprovalEventPayload);
            String restaurantApprovalRequestTopicName = orderServiceConfigData.getRestaurantApprovalRequestTopicName();
            
            kafkaProducer.send(restaurantApprovalRequestTopicName,
                               sagaId,
                               restaurantApprovalRequestAvroModel,
                               kafkaMessageHelper.getKafkaCallback(restaurantApprovalRequestTopicName,
                                                                   restaurantApprovalRequestAvroModel,
                                                                   orderApprovalOutboxMessage,
                                                                   outboxCallback,
                                                                   orderApprovalEventPayload.getOrderId(),
                                                                   "RestaurantApprovalRequestAvroModel"));
            
            log.info("OrderApprovalEventPayload send to Kafka for order id : {} and saga id : {} ", orderApprovalEventPayload.getOrderId(), sagaId);
        } catch (Exception e) {
            log.error("Error while sending OrderApprovalEventPayload to kafka with order id : {} and saga id : {} , error : {} ",
                      orderApprovalEventPayload.getOrderId(), sagaId, e);
            
        }
    }
    
}
