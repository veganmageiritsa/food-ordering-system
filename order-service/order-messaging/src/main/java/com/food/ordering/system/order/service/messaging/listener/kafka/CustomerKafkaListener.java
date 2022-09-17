package com.food.ordering.system.order.service.messaging.listener.kafka;

import java.util.List;

import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.food.ordering.system.kafka.consumer.KafkaConsumer;
import com.food.ordering.system.kafka.order.avro.model.CustomerAvroModel;
import com.food.ordering.system.order.service.domain.ports.input.message.listener.customer.CustomerMessageListener;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CustomerKafkaListener implements KafkaConsumer<CustomerAvroModel> {
    
    private final CustomerMessageListener customerMessageListener;
    
    private final OrderMessagingDataMapper orderMessagingDataMapper;
    
    public CustomerKafkaListener(
        final CustomerMessageListener customerMessageListener,
        final OrderMessagingDataMapper orderMessagingDataMapper) {
        this.customerMessageListener = customerMessageListener;
        this.orderMessagingDataMapper = orderMessagingDataMapper;
    }
    
    @Override
    public void receive(
        @Payload final List<CustomerAvroModel> messages,
        @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) final List<String> keys,
        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) final List<Integer> partitions,
        @Header(KafkaHeaders.OFFSET) final List<Long> offsets) {
        
        log.info("{} number of customer create messages received with keys:{}, partitions:{} and offsets: {}",
                 messages.size(),
                 keys.toString(),
                 partitions.toString(),
                 offsets.toString());
        
        messages.forEach(customerAvroModel ->
                             customerMessageListener.customerCreated(orderMessagingDataMapper.customerAvroModelToCustomerModel(customerAvroModel)));
    }
    
}
