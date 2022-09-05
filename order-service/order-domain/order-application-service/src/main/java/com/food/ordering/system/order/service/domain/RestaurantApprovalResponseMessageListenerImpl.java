package com.food.ordering.system.order.service.domain;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.food.ordering.system.order.service.domain.dto.message.RestaurantApprovalResponse;
import com.food.ordering.system.order.service.domain.ports.input.message.listener.restaurantapproval.RestaurantApprovalResponseMessageListener;
import lombok.extern.slf4j.Slf4j;

@Validated
@Slf4j
@Service
public class RestaurantApprovalResponseMessageListenerImpl implements RestaurantApprovalResponseMessageListener {
    
    @Override
    public void orderApproved(final RestaurantApprovalResponse restaurantApprovalResponse) {
    
    }
    
    @Override
    public void orderRejected(final RestaurantApprovalResponse restaurantApprovalResponse) {
    
    }
    
}
