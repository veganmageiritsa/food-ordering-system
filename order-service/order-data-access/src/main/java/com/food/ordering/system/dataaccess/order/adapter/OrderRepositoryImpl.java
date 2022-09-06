package com.food.ordering.system.dataaccess.order.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.food.ordering.system.dataaccess.order.mapper.OrderDataAccessMapper;
import com.food.ordering.system.dataaccess.order.repository.OrderJpaRepository;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.order.service.domain.valueobject.TrackingId;

@Component
public class OrderRepositoryImpl implements OrderRepository {
    
    private final OrderJpaRepository orderJpaRepository;
    
    private final OrderDataAccessMapper orderDataAccessMapper;
    
    public OrderRepositoryImpl(
        final OrderJpaRepository orderJpaRepository,
        final OrderDataAccessMapper orderDataAccessMapper) {
        this.orderJpaRepository = orderJpaRepository;
        this.orderDataAccessMapper = orderDataAccessMapper;
    }
    
    @Override
    public Order save(final Order order) {
        return orderDataAccessMapper
            .orderEntityToOrder(
                orderJpaRepository
                    .save(orderDataAccessMapper
                              .orderToOrderEntity(order)));
        
    }
    
    @Override
    public Optional<Order> findByTrackingId(final TrackingId trackingId) {
        return orderJpaRepository.findByTrackingId(trackingId.getValue())
                                 .map(orderDataAccessMapper::orderEntityToOrder);
    }
    
}
