package com.food.ordering.system.order.service.domain.mapper;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.domain.valueobject.Money;
import com.food.ordering.system.domain.valueobject.OrderId;
import com.food.ordering.system.domain.valueobject.ProductId;
import com.food.ordering.system.domain.valueobject.RestaurantId;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.dto.create.OrderAddress;
import com.food.ordering.system.order.service.domain.dto.create.OrderItems;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.OrderItem;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.valueobject.StreetAddress;

@Component
public class OrderDataMapper {
    
    public Restaurant createOrderCommandToRestaurant(CreateOrderCommand createOrderCommand) {
        return Restaurant.newBuilder()
                         .restaurantId(new RestaurantId(createOrderCommand.getRestaurantId()))
                         .products(createOrderCommand.getOrderItems()
                                                     .stream()
                                                     .map(orderItem -> new Product(new ProductId(orderItem.getProductId())))
                                                     .collect(Collectors.toList()))
                         .build();
    }
    
    public Order createOrderCommandToOrder(CreateOrderCommand createOrderCommand) {
        return Order.builder()
                    .customerId(new CustomerId(createOrderCommand.getCustomerId()))
                    .restaurantId(new RestaurantId(createOrderCommand.getRestaurantId()))
                    .streetAddress(orderAddressToStreetAddress(createOrderCommand.getOrderAddress()))
                    .price(new Money(createOrderCommand.getPrice()))
                    .orderItems(orderItemsToOrderItemEntities(createOrderCommand.getOrderItems()))
                    .build();
    }
    
    public CreateOrderResponse orderToCreateOrderResponse(Order order, String message) {
        return CreateOrderResponse.builder()
                                  .orderStatus(order.getOrderStatus())
                                  .orderTrackingId(order.getTrackingId().getValue())
                                  .message(message)
                                  .build();
    }
    
    private List<OrderItem> orderItemsToOrderItemEntities(final List<OrderItems> orderItems) {
        return orderItems.stream()
                         .map(orderItem -> OrderItem.builder()
                                                    .product(new Product(new ProductId(orderItem.getProductId())))
                                                    .price(new Money(orderItem.getPrice()))
                                                    .quantity(orderItem.getQuantity())
                                                    .subTotal(new Money(orderItem.getSubTotal()))
                                                    .build())
                         .collect(Collectors.toList());
    }
    
    private StreetAddress orderAddressToStreetAddress(final OrderAddress orderAddress) {
        return new StreetAddress(
            UUID.randomUUID(),
            orderAddress.getStreet(),
            orderAddress.getPostalCode(),
            orderAddress.getCity());
    }
    
}
