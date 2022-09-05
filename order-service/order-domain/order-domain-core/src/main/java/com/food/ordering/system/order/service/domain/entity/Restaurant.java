package com.food.ordering.system.order.service.domain.entity;

import java.util.List;

import com.food.ordering.system.domain.entity.AggregateRoot;
import com.food.ordering.system.domain.valueobject.RestaurantId;

public class Restaurant extends AggregateRoot<RestaurantId> {

    private final List<Product> products;
    private boolean active;
    
    private Restaurant(final Builder builder) {
        setId(builder.restaurantId);
        products = builder.products;
        active = builder.active;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static final class Builder {
        
        private RestaurantId restaurantId;
        
        private List<Product> products;
        
        private boolean active;
        
        private Builder() {
        }
        
        public Builder restaurantId(final RestaurantId val) {
            restaurantId = val;
            return this;
        }
        
        public Builder products(final List<Product> val) {
            products = val;
            return this;
        }
        
        public Builder active(final boolean val) {
            active = val;
            return this;
        }
        
        public Restaurant build() {
            return new Restaurant(this);
        }
        
    }
    
    public List<Product> getProducts() {
        return products;
    }
    
    public boolean isActive() {
        return active;
    }
    
}
