package com.food.ordering.system.restaurant.service.domain.valueobject;

import java.util.UUID;

import com.food.ordering.system.domain.valueobject.BaseId;

public class OrderApprovalId extends BaseId<UUID> {
    
    public OrderApprovalId(UUID value) {
        super(value);
    }
    
}
