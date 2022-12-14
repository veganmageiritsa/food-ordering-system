package com.food.ordering.system.dataaccess.customer.mapper;

import org.springframework.stereotype.Component;

import com.food.ordering.system.dataaccess.customer.entity.CustomerEntity;
import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.order.service.domain.entity.Customer;

@Component
public class CustomerDataAccessMapper {
    
    public Customer customerEntityToCustomer(CustomerEntity customerEntity) {
        return new Customer(new CustomerId(customerEntity.getId()), customerEntity.getUserName(), customerEntity.getLastName(), customerEntity.getFirstName());
    }
    
    public CustomerEntity customerToCustomerEntity(Customer customer) {
        return CustomerEntity.builder()
                             .id(customer.getId().getValue())
                             .userName(customer.getUserName())
                             .firstName(customer.getFirstName())
                             .lastName(customer.getLastName())
                             .build();
    }
    
}
