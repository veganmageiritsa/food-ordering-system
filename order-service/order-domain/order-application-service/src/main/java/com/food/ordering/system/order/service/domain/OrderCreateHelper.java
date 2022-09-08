package com.food.ordering.system.order.service.domain;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.payment.OrderCreatedPaymentRequestMessagePublisher;
import com.food.ordering.system.order.service.domain.ports.output.repository.CustomerRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.RestaurantRepository;
import com.food.ordering.system.order.service.domain.service.OrderDomainService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrderCreateHelper {
    
    private final OrderDomainService orderDomainService;
    
    private final OrderRepository orderRepository;
    
    private final CustomerRepository customerRepository;
    
    private final RestaurantRepository restaurantRepository;
    
    private final OrderDataMapper orderDataMapper;
    
    private final OrderCreatedPaymentRequestMessagePublisher orderCreatedEventDomainEventPublisher;
    
    public OrderCreateHelper(
        final OrderDomainService orderDomainService,
        final OrderRepository orderRepository,
        final CustomerRepository customerRepository,
        final RestaurantRepository restaurantRepository,
        final OrderDataMapper orderDataMapper,
        final OrderCreatedPaymentRequestMessagePublisher orderCreatedEventDomainEventPublisher) {
        this.orderDomainService = orderDomainService;
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.restaurantRepository = restaurantRepository;
        this.orderDataMapper = orderDataMapper;
        this.orderCreatedEventDomainEventPublisher = orderCreatedEventDomainEventPublisher;
    }
    
    @Transactional
    public OrderCreatedEvent createOrder(CreateOrderCommand createOrderCommand) {
        checkCustomer(createOrderCommand.getCustomerId());
        Restaurant restaurant = checkRestaurant(createOrderCommand);
        Order order = orderDataMapper.createOrderCommandToOrder(createOrderCommand);
        OrderCreatedEvent orderCreatedEvent = orderDomainService.validateAndInitiateOrder(order, restaurant, orderCreatedEventDomainEventPublisher);
        saveOrder(order);
        return orderCreatedEvent;
    }
    
    private void checkCustomer(final UUID customerId) {
        customerRepository.findCustomer(customerId)
                          .orElseThrow(() -> new OrderDomainException("Customer with id not found: " + customerId));
    }
    
    private Restaurant checkRestaurant(final CreateOrderCommand createOrderCommand) {
        Restaurant restaurant = orderDataMapper.createOrderCommandToRestaurant(createOrderCommand);
        return restaurantRepository.findRestaurantInformation(restaurant)
                                   .orElseThrow(
                                       () -> new OrderDomainException("Customer with id not found: " + restaurant.getId().getValue()));
    }
    
    private Order saveOrder(Order order) {
        Order persistedOrder = orderRepository.save(order);
        log.info("Order is saved with id: {}", persistedOrder.getId().getValue());
        return persistedOrder;
    }
    
}
