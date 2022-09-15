package com.food.ordering.system.restaurant.service.domain;

import org.springframework.stereotype.Service;

import com.food.ordering.system.restaurant.service.domain.dto.RestaurantApprovalRequest;
import com.food.ordering.system.restaurant.service.domain.ports.input.RestaurantApprovalRequestMessageListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RestaurantApprovalRequestMessageListenerImpl implements RestaurantApprovalRequestMessageListener {
    
    private final RestaurantApprovalRequestHelper restaurantApprovalRequestHelper;
    
    public RestaurantApprovalRequestMessageListenerImpl(
        final RestaurantApprovalRequestHelper restaurantApprovalRequestHelper) {
        this.restaurantApprovalRequestHelper = restaurantApprovalRequestHelper;
    }
    
    @Override
    public void approveOrder(final RestaurantApprovalRequest restaurantApprovalRequest) {
        restaurantApprovalRequestHelper.persistOrderApproval(restaurantApprovalRequest);
    }
    
}
