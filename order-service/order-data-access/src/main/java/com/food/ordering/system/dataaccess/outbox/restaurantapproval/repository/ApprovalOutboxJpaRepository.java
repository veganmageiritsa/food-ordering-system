package com.food.ordering.system.dataaccess.outbox.restaurantapproval.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.food.ordering.system.dataaccess.outbox.restaurantapproval.entity.ApprovalOutboxEntity;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;

@Repository
public interface ApprovalOutboxJpaRepository extends JpaRepository<ApprovalOutboxEntity, UUID> {
    
    Optional<List<ApprovalOutboxEntity>> findByTypeAndOutboxStatusAndSagaStatusIn(
        String type,
        OutboxStatus outboxStatus,
        List<SagaStatus> sagaStatus);
    
    Optional<ApprovalOutboxEntity> findByTypeAndSagaIdAndSagaStatusIn(
        String type,
        UUID sagaId,
        List<SagaStatus> sagaStatus);
    
    void deleteByTypeAndOutboxStatusAndSagaStatusIn(
        String type,
        OutboxStatus outboxStatus,
        List<SagaStatus> sagaStatus);
    
}
