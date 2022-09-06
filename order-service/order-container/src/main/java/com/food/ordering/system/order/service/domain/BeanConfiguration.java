package com.food.ordering.system.order.service.domain;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.food.ordering.system.order.service.domain.service.OrderDomainService;
import com.food.ordering.system.order.service.domain.service.OrderDomainServiceImpl;

@Configuration
public class BeanConfiguration {
    
    @Bean
    public OrderDomainService orderDomainService() {
        return new OrderDomainServiceImpl();
    }
    
}
