package com.food.ordering.system.dataaccess.order.mapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.food.ordering.system.dataaccess.order.entity.OrderAddressEntity;
import com.food.ordering.system.dataaccess.order.entity.OrderEntity;
import com.food.ordering.system.dataaccess.order.entity.OrderItemEntity;
import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.domain.valueobject.Money;
import com.food.ordering.system.domain.valueobject.OrderId;
import com.food.ordering.system.domain.valueobject.ProductId;
import com.food.ordering.system.domain.valueobject.RestaurantId;
import com.food.ordering.system.order.service.domain.entity.Order;
import static com.food.ordering.system.order.service.domain.entity.Order.FAILURE_MESSAGE_DELIMITER;
import com.food.ordering.system.order.service.domain.entity.OrderItem;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.valueobject.OrderItemId;
import com.food.ordering.system.order.service.domain.valueobject.StreetAddress;
import com.food.ordering.system.order.service.domain.valueobject.TrackingId;

@Component
public class OrderDataAccessMapper {
    
    public OrderEntity orderToOrderEntity(Order order) {
        OrderEntity orderEntity = OrderEntity.builder()
                                             .id(order.getId().getValue())
                                             .customerId(order.getCustomerId().getValue())
                                             .restaurantId(order.getRestaurantId().getValue())
                                             .trackingId(order.getTrackingId().getValue())
                                             .address(deliveryAddressToAddressEntity(order.getStreetAddress()))
                                             .price(order.getPrice().getAmount())
                                             .orderItems(orderItemsToOrderItemEntities(order.getOrderItems()))
                                             .orderStatus(order.getOrderStatus())
                                             .failureMessages(order.getFailureMessages() != null
                                                              ? String.join(FAILURE_MESSAGE_DELIMITER, order.getFailureMessages())
                                                              : "")
                                             .build();
        orderEntity.getAddress().setOrder(orderEntity);
        orderEntity.getOrderItems().forEach(orderItemEntity -> orderItemEntity.setOrder(orderEntity));
        return orderEntity;
    }
    
    public Order orderEntityToOrder(OrderEntity orderEntity) {
        return Order.builder()
                    .id(new OrderId(orderEntity.getId()))
                    .customerId(new CustomerId(orderEntity.getCustomerId()))
                    .restaurantId(new RestaurantId(orderEntity.getRestaurantId()))
                    .streetAddress(addressEntityToDeliveryAddress(orderEntity.getAddress()))
                    .price(new Money(orderEntity.getPrice()))
                    .orderItems(orderItemEntitiesToOrderItems(orderEntity.getOrderItems()))
                    .trackingId(new TrackingId(orderEntity.getTrackingId()))
                    .orderStatus(orderEntity.getOrderStatus())
                    .failureMessages(orderEntity.getFailureMessages().isEmpty() ? new ArrayList<>() :
                                     new ArrayList<>(Arrays.asList(orderEntity.getFailureMessages()
                                                                              .split(FAILURE_MESSAGE_DELIMITER))))
                    .build();
    }
    
    private List<OrderItemEntity> orderItemsToOrderItemEntities(final List<OrderItem> orderItems) {
        return orderItems
            .stream()
            .map(this::orderItemToOrderItemEntity)
            .collect(Collectors.toList());
    }
    
    private OrderItemEntity orderItemToOrderItemEntity(final OrderItem orderItem) {
        return OrderItemEntity.builder()
                              .id(orderItem.getId().getValue())
                              .price(orderItem.getPrice().getAmount())
                              .quantity(orderItem.getQuantity())
                              .subTotal(orderItem.getSubTotal().getAmount())
                              .productId(orderItem.getProduct().getId().getValue())
                              .build();
    }
    
    private OrderAddressEntity deliveryAddressToAddressEntity(final StreetAddress streetAddress) {
        return OrderAddressEntity.builder()
                                 .city(streetAddress.getCity())
                                 .postalCode(streetAddress.getPostalCode())
                                 .street(streetAddress.getStreet())
                                 .id(streetAddress.getId())
                                 .build();
    }
    
    private List<OrderItem> orderItemEntitiesToOrderItems(List<OrderItemEntity> items) {
        return items.stream()
                    .map(orderItemEntity -> OrderItem.builder()
                                                     .id(new OrderItemId(orderItemEntity.getId()))
                                                     .product(new Product(new ProductId(orderItemEntity.getProductId())))
                                                     .price(new Money(orderItemEntity.getPrice()))
                                                     .quantity(orderItemEntity.getQuantity())
                                                     .subTotal(new Money(orderItemEntity.getSubTotal()))
                                                     .build())
                    .collect(Collectors.toList());
    }
    
    private StreetAddress addressEntityToDeliveryAddress(OrderAddressEntity address) {
        return new StreetAddress(address.getId(),
                                 address.getStreet(),
                                 address.getPostalCode(),
                                 address.getCity());
    }
    
}
