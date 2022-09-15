package com.food.ordering.system.order.service.domain;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.food.ordering.system.order.service.domain.dto.message.RestaurantApprovalResponse;
import static com.food.ordering.system.order.service.domain.entity.Order.FAILURE_MESSAGE_DELIMITER;
import com.food.ordering.system.order.service.domain.ports.input.message.listener.restaurantapproval.RestaurantApprovalResponseMessageListener;
import lombok.extern.slf4j.Slf4j;

@Validated
@Slf4j
@Service
public class RestaurantApprovalResponseMessageListenerImpl implements RestaurantApprovalResponseMessageListener {
    
    private final OrderApprovalSaga orderApprovalSaga;
    
    public RestaurantApprovalResponseMessageListenerImpl(final OrderApprovalSaga orderApprovalSaga) {
        this.orderApprovalSaga = orderApprovalSaga;
    }
    
    @Override
    public void orderApproved(final RestaurantApprovalResponse restaurantApprovalResponse) {
        orderApprovalSaga.process(restaurantApprovalResponse);
        log.info("order approved with id: {}", restaurantApprovalResponse.getOrderId());
        
    }
    
    @Override
    public void orderRejected(final RestaurantApprovalResponse restaurantApprovalResponse) {
        orderApprovalSaga.rollback(restaurantApprovalResponse);
        log.info(" OrderApproval Saga rollback operation for order  with id: {} cancelled  with failure messages : {}",
                 restaurantApprovalResponse.getOrderId(),
                 String.join(FAILURE_MESSAGE_DELIMITER, restaurantApprovalResponse.getFailureMessages()));
    }
    
}
