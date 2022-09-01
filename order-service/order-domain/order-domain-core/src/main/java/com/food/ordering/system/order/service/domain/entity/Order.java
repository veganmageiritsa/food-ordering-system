package com.food.ordering.system.order.service.domain.entity;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.food.ordering.system.domain.entity.AggregateRoot;
import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.domain.valueobject.Money;
import com.food.ordering.system.domain.valueobject.OrderId;
import com.food.ordering.system.domain.valueobject.OrderStatus;
import com.food.ordering.system.domain.valueobject.RestaurantId;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.valueobject.OrderItemId;
import com.food.ordering.system.order.service.domain.valueobject.StreetAddress;
import com.food.ordering.system.order.service.domain.valueobject.TrackingId;

public class Order extends AggregateRoot<OrderId> {
    
    private final CustomerId customerId;
    
    private final RestaurantId restaurantId;
    
    private final StreetAddress streetAddress;
    
    private final Money price;
    
    private final List<OrderItem> orderItems;
    
    private TrackingId trackingId;
    
    private OrderStatus orderStatus;
    
    private List<String> failureMessages;
    
    public void initializeOrder() {
        setId(new OrderId(UUID.randomUUID()));
        trackingId = new TrackingId(UUID.randomUUID());
        orderStatus = OrderStatus.PENDING;
        initializeOrderItems();
    }
    
    private void initializeOrderItems() {
        long itemId = 1;
        for (OrderItem orderItem : orderItems) {
            orderItem.initializeOrderItem(super.getId(), new OrderItemId(itemId++));
            
        }
    }
    
    public void validateOrder() {
        validateInitialOrder();
        validateTotalPrice();
        validateItemsPrice();
    }
    
    private void validateItemsPrice() {
        Money orderItemsTotal = orderItems
            .stream()
            .map(orderItem -> {
                validateItemPrice(orderItem);
                return orderItem.getSubTotal();
            }).reduce(Money.ZERO, Money::add);
        
        if (!price.equals(orderItemsTotal)) {
            throw new OrderDomainException("Inalid Total Price");
        }
    }
    
    private void validateItemPrice(final OrderItem orderItem) {
        if (!orderItem.isPriceValid()) {
            throw new OrderDomainException("Order Item price is not valid");
        }
    }
    
    private void validateTotalPrice() {
        if (price == null || !price.isGreaterThanZero()) {
            throw new OrderDomainException("invalid price");
        }
    }
    
    private void validateInitialOrder() {
        if (!(orderStatus == null && getId() == null)) {
            throw new OrderDomainException("order is not in correct state for initialization");
        }
    }
    
    public void pay() {
        if (orderStatus.equals(OrderStatus.PENDING)) {
            orderStatus = OrderStatus.PAID;
            return;
        }
        throw new OrderDomainException("Order is not in correct state for pay operation");
    }
    
    public void approve() {
        if (orderStatus.equals(OrderStatus.PAID)) {
            orderStatus = OrderStatus.APPROVED;
        }
        else {
            throw new OrderDomainException("Order is not in correct state for approval operation");
        }
    }
    
    public void initCancelling(List<String> failureMessages){
        if(orderStatus != OrderStatus.PAID){
            throw new OrderDomainException("Order is not in correct state for cancelling operation");
        }
        orderStatus=OrderStatus.CANCELLING;
        updateFailureMessages(failureMessages);
    
    }
    
    public void cancel(List<String> failureMessages){
        if(!(orderStatus == OrderStatus.PENDING || orderStatus==OrderStatus.CANCELLING)){
            throw new OrderDomainException("Order is not in correct state for cancelling operation");
        }
        orderStatus=OrderStatus.CANCELLED;
        updateFailureMessages(failureMessages);
    }
    
    private void updateFailureMessages(final List<String> failureMessages) {
        if(this.failureMessages != null && failureMessages !=null){
            this.failureMessages.addAll(failureMessages.stream().filter(failureMessage-> !failureMessage.isEmpty()).collect(Collectors.toList()));
        }
        if(this.failureMessages == null){
            this.failureMessages = failureMessages;
        }
    }
    
    private Order(final Builder builder) {
        super.setId(builder.orderId);
        customerId = builder.customerId;
        restaurantId = builder.restaurantId;
        streetAddress = builder.streetAddress;
        price = builder.price;
        orderItems = builder.orderItems;
        trackingId = builder.trackingId;
        orderStatus = builder.orderStatus;
        failureMessages = builder.failureMessages;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static final class Builder {
        
        private OrderId orderId;
        
        private CustomerId customerId;
        
        private RestaurantId restaurantId;
        
        private StreetAddress streetAddress;
        
        private Money price;
        
        private List<OrderItem> orderItems;
        
        private TrackingId trackingId;
        
        private OrderStatus orderStatus;
        
        private List<String> failureMessages;
        
        private Builder() {
        }
        
        public Builder id(final OrderId val) {
            orderId = val;
            return this;
        }
        
        public Builder customerId(final CustomerId val) {
            customerId = val;
            return this;
        }
        
        public Builder restaurantId(final RestaurantId val) {
            restaurantId = val;
            return this;
        }
        
        public Builder streetAddress(final StreetAddress val) {
            streetAddress = val;
            return this;
        }
        
        public Builder price(final Money val) {
            price = val;
            return this;
        }
        
        public Builder orderItems(final List<OrderItem> val) {
            orderItems = val;
            return this;
        }
        
        public Builder trackingId(final TrackingId val) {
            trackingId = val;
            return this;
        }
        
        public Builder orderStatus(final OrderStatus val) {
            orderStatus = val;
            return this;
        }
        
        public Builder failureMessages(final List<String> val) {
            failureMessages = val;
            return this;
        }
        
        public Order build() {
            return new Order(this);
        }
        
    }
    
    public CustomerId getCustomerId() {
        return customerId;
    }
    
    public RestaurantId getRestaurantId() {
        return restaurantId;
    }
    
    public StreetAddress getStreetAddress() {
        return streetAddress;
    }
    
    public Money getPrice() {
        return price;
    }
    
    public List<OrderItem> getOrderItems() {
        return orderItems;
    }
    
    public TrackingId getTrackingId() {
        return trackingId;
    }
    
    public OrderStatus getOrderStatus() {
        return orderStatus;
    }
    
    public List<String> getFailureMessages() {
        return failureMessages;
    }
    
}
