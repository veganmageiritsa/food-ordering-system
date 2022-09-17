package com.food.ordering.system.restaurant.service.domain.outbox.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrderOutboxCleanerScheduler implements OutboxScheduler {
    
    private final OrderOutboxHelper orderOutboxHelper;
    
    public OrderOutboxCleanerScheduler(OrderOutboxHelper orderOutboxHelper) {
        this.orderOutboxHelper = orderOutboxHelper;
    }
    
    @Transactional
    @Scheduled(cron = "@midnight")
    @Override
    public void processOutboxMessage() {
        orderOutboxHelper.getOrderOutboxMessageByOutboxStatus(OutboxStatus.COMPLETED)
                         .filter(orderOutboxMessages -> !orderOutboxMessages.isEmpty())
                         .ifPresent(outboxMessages -> {
                             log.info("Received {} OrderOutboxMessage for clean-up!", outboxMessages.size());
                             orderOutboxHelper.deleteOrderOutboxMessageByOutboxStatus(OutboxStatus.COMPLETED);
                             log.info("Deleted {} OrderOutboxMessage!", outboxMessages.size());
                         });
    }
    
}
