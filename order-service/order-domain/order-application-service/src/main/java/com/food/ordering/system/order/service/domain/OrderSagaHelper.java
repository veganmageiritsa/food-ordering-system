package com.food.ordering.system.order.service.domain;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.food.ordering.system.domain.valueobject.OrderId;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.exception.OrderNotFoundException;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrderSagaHelper {
    
    private final OrderRepository orderRepository;
    
    public OrderSagaHelper(final OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    
    public Order findOrder(final String orderId) {
        
        Optional<Order> orderResponse = orderRepository.findByOrderId(new OrderId(UUID.fromString(orderId)));
        if (orderResponse.isEmpty()) {
            log.error("Order with id: {} could not be found", orderId);
            throw new OrderNotFoundException("Order with id " + orderId + " could not be found");
        }
        return orderResponse.get();
    }
    
    public void save(Order order) {
        orderRepository.save(order);
    }
    
}