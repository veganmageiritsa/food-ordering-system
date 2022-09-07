package com.food.ordering.system.payment.service.domain.valueobject;

import java.util.UUID;

import com.food.ordering.system.domain.valueobject.BaseId;

public class CreditHistoryId extends BaseId<UUID> {
    
    public CreditHistoryId(final UUID value) {
        super(value);
    }
    
}
