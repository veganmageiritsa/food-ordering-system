package com.food.ordering.system.order.service.domain.entity;

import com.food.ordering.system.domain.entity.AggregateRoot;
import com.food.ordering.system.domain.valueobject.CustomerId;


public class Customer extends AggregateRoot<CustomerId> {
    
    private String userName;
    
    private String lastName;
    
    private String firstName;
    
    public Customer() {
    }
    
    public Customer(final CustomerId customerId) {
        setId(customerId);
    }
    
    public Customer(final CustomerId customerId, final String userName, final String lastName, final String firstName) {
        setId(customerId);
        this.userName = userName;
        this.lastName = lastName;
        this.firstName = firstName;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
}
