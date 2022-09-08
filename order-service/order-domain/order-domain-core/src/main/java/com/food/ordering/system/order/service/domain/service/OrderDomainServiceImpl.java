package com.food.ordering.system.order.service.domain.service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static com.food.ordering.system.domain.DomainConstants.UTC;
import com.food.ordering.system.domain.event.publisher.DomainEventPublisher;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrderDomainServiceImpl implements OrderDomainService {
    
    @Override
    public OrderCreatedEvent validateAndInitiateOrder(
        Order order, Restaurant restaurant,
        final DomainEventPublisher<OrderCreatedEvent> orderCreatedEventDomainEventPublisher) {
        validateRestaurant(restaurant);
        setOrderProductInformation(order, restaurant);
        order.validateOrder();
        order.initializeOrder();
        log.info("Order with id: {} is initiated", order.getId().getValue());
        return new OrderCreatedEvent(order, ZonedDateTime.now(ZoneId.of(UTC)), orderCreatedEventDomainEventPublisher);
    }
    
    
    @Override
    public OrderPaidEvent payOrder(
        final Order order,
        final DomainEventPublisher<OrderPaidEvent> orderCreatedEventDomainEventPublisher) {
        order.pay();
        log.info("Order with id: {} is paid", order.getId().getValue());
        return new OrderPaidEvent(order, ZonedDateTime.now(ZoneId.of(UTC)), orderCreatedEventDomainEventPublisher);
        
    }
    
    @Override
    public void approveOrder(final Order order) {
        order.approve();
        log.info("Order with id: {} is approved", order.getId().getValue());
        
    }
    
    @Override
    public OrderCancelledEvent cancelOrderPayment(
        final Order order, final List<String> failureMessages,
        final DomainEventPublisher<OrderCancelledEvent> orderCancelledEventDomainEventPublisher) {
        order.initCancelling(failureMessages);
        log.info("Order payment cancelled for order with id: {}", order.getId().getValue());
        return new OrderCancelledEvent(order, ZonedDateTime.now(ZoneId.of(UTC)), orderCancelledEventDomainEventPublisher);
    }
    
    @Override
    public void cancelOrder(final Order order, final List<String> failureMessages) {
        order.cancel(failureMessages);
        log.info("Order is cancelled for order with id: {}", order.getId().getValue());
    }
    
    private void validateRestaurant(final Restaurant restaurant) {
        if (!restaurant.isActive()) {
            throw new OrderDomainException("Restaurant is closed " + restaurant.getId().getValue());
        }
    }
    
    private void setOrderProductInformation(final Order order, final Restaurant restaurant) {
        //        Map<UUID, Product> products = restaurant.getProducts()
        //                                                .stream()
        //                                                .collect(Collectors.toMap(product -> product.getId().getValue(), Function.identity()));
        //
        //        order.getOrderItems()
        //             .forEach(orderItem -> Optional.ofNullable(products.get(orderItem.getOrderId().getValue()))
        //                                           .ifPresent(
        //                                               product -> orderItem.getProduct().updateWithConfirmedNameAndPrice(product.getName(), product
        //                                               .getPrice())));
        order.getOrderItems()
             .forEach(orderItem -> restaurant.getProducts().forEach(product -> {
                 Product orderItemProduct = orderItem.getProduct();
                 if (orderItemProduct.equals(product)) {
                     orderItemProduct.updateWithConfirmedNameAndPrice(product.getName(), product.getPrice());
                 }
             }));
    }
    
}
