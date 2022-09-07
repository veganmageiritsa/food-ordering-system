package com.food.ordering.system.payment.service.domain.entity;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.food.ordering.system.domain.entity.AggregateRoot;
import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.domain.valueobject.Money;
import com.food.ordering.system.domain.valueobject.OrderId;
import com.food.ordering.system.domain.valueobject.PaymentStatus;
import com.food.ordering.system.payment.service.domain.valueobject.PaymentId;

public class Payment extends AggregateRoot<PaymentId> {
    
    private final OrderId orderId;
    
    private final CustomerId customerId;
    
    private final Money price;
    
    private PaymentStatus paymentStatus;
    
    private ZonedDateTime createdAt;
    
    
    public void initializePayment() {
        setId(new PaymentId(UUID.randomUUID()));
        createdAt = ZonedDateTime.now(ZoneId.of("UTC"));
    }
    
    public void validatePayment(List<String> failureMessages) {
        if (price == null || !price.isGreaterThanZero()) {
            failureMessages.add("Total Price must be greater than zero");
        }
    }
    
    public void updateStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    
    private Payment(final Builder builder) {
        setId(builder.paymentId);
        orderId = builder.orderId;
        customerId = builder.customerId;
        price = builder.price;
        paymentStatus = builder.paymentStatus;
        setCreatedAt(builder.createdAt);
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static final class Builder {
        
        private PaymentId paymentId;
        
        private OrderId orderId;
        
        private CustomerId customerId;
        
        private Money price;
        
        private PaymentStatus paymentStatus;
        
        private ZonedDateTime createdAt;
        
        private Builder() {
        }
        
        public Builder id(final PaymentId val) {
            paymentId = val;
            return this;
        }
        
        public Builder orderId(final OrderId val) {
            orderId = val;
            return this;
        }
        
        public Builder customerId(final CustomerId val) {
            customerId = val;
            return this;
        }
        
        public Builder price(final Money val) {
            price = val;
            return this;
        }
        
        public Builder paymentStatus(final PaymentStatus val) {
            paymentStatus = val;
            return this;
        }
        
        public Builder createdAt(final ZonedDateTime val) {
            createdAt = val;
            return this;
        }
        
        public Payment build() {
            return new Payment(this);
        }
        
    }
    
    public OrderId getOrderId() {
        return orderId;
    }
    
    public CustomerId getCustomerId() {
        return customerId;
    }
    
    public Money getPrice() {
        return price;
    }
    
    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }
    
    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(final ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    
}
