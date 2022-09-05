package com.food.ordering.system.order.service.domain;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.order.service.domain.dto.track.TrackOrderQuery;
import com.food.ordering.system.order.service.domain.dto.track.TrackOrderResponse;
import com.food.ordering.system.order.service.domain.exception.OrderNotFoundException;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.order.service.domain.valueobject.TrackingId;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrderTrackCommandHandler {
    
    private final OrderDataMapper orderDataMapper;
    
    private final OrderRepository orderRepository;
    
    public OrderTrackCommandHandler(
        final OrderDataMapper orderDataMapper,
        final OrderRepository orderRepository) {
        this.orderDataMapper = orderDataMapper;
        this.orderRepository = orderRepository;
    }
    
    @Transactional(readOnly = true)
    public TrackOrderResponse trackOrder(TrackOrderQuery trackOrderQuery) {
        return orderRepository.findByTrackingId(new TrackingId(trackOrderQuery.getOrderTrackingId()))
                              .map(orderDataMapper::orderToTrackOrderResponse)
                              .orElseThrow(() -> new OrderNotFoundException("Order Not Found with id: " + trackOrderQuery.getOrderTrackingId()));
    }
    
}
