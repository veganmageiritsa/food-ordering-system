package com.food.ordering.system.payment.service.domain.entity;

import com.food.ordering.system.domain.entity.BaseEntity;
import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.domain.valueobject.Money;
import com.food.ordering.system.payment.service.domain.valueobject.CreditEntryId;

public class CreditEntry extends BaseEntity<CreditEntryId> {
    
    private final CustomerId customerId;
    
    private Money totalCreditAmount;
    
    public void addCreditAmount(Money amount) {
        totalCreditAmount.add(amount);
    }
    
    public void subtractCreditAmount(Money amount) {
        totalCreditAmount.subtract(amount);
    }
    
    private CreditEntry(final Builder builder) {
        setId(builder.creditEntryId);
        customerId = builder.customerId;
        totalCreditAmount = builder.totalCreditAmount;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static final class Builder {
        
        private CreditEntryId creditEntryId;
        
        private CustomerId customerId;
        
        private Money totalCreditAmount;
        
        private Builder() {
        }
        
        public Builder id(final CreditEntryId val) {
            creditEntryId = val;
            return this;
        }
        
        public Builder customerId(final CustomerId val) {
            customerId = val;
            return this;
        }
        
        public Builder totalCreditAmount(final Money val) {
            totalCreditAmount = val;
            return this;
        }
        
        public CreditEntry build() {
            return new CreditEntry(this);
        }
        
    }
    
    
    public CustomerId getCustomerId() {
        return customerId;
    }
    
    public Money getTotalCreditAmount() {
        return totalCreditAmount;
    }
    
}
