package com.food.ordering.system.dataaccess.outbox.restaurantapproval.exception;

public class ApprovalOutboxNotFoundException extends RuntimeException {
    
    public ApprovalOutboxNotFoundException(String message) {
        super(message);
    }
    
}
