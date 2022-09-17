package com.food.ordering.system.domain;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.domain.valueobject.Money;
import com.food.ordering.system.domain.valueobject.OrderId;
import com.food.ordering.system.domain.valueobject.OrderStatus;
import static com.food.ordering.system.domain.valueobject.OrderStatus.PENDING;
import com.food.ordering.system.domain.valueobject.PaymentOrderStatus;
import com.food.ordering.system.domain.valueobject.ProductId;
import com.food.ordering.system.domain.valueobject.RestaurantId;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.dto.create.OrderAddress;
import com.food.ordering.system.order.service.domain.dto.create.OrderItems;
import com.food.ordering.system.order.service.domain.entity.Customer;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentEventPayload;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.input.service.OrderApplicationService;
import com.food.ordering.system.order.service.domain.ports.output.repository.CustomerRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.PaymentOutboxRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.RestaurantRepository;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import static com.food.ordering.system.saga.order.SagaConstants.ORDER_SAGA_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = OrderTestConfiguration.class)
class OrderApplicationServiceTest {
    
    @Autowired
    private OrderApplicationService orderApplicationService;
    
    @Autowired
    private OrderDataMapper orderDataMapper;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private RestaurantRepository restaurantRepository;
    
    @Autowired
    private PaymentOutboxRepository paymentOutboxRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private CreateOrderCommand createOrderCommand;
    
    private CreateOrderCommand createOrderCommandWrongPrice;
    
    private CreateOrderCommand createOrderCommandWrongProductPrice;
    
    private final UUID CUSTOMER_ID = UUID.fromString("5fc03087-d265-11e7-b8c6-83e29cd24f4c");
    
    private final UUID RESTAURANT_ID = UUID.fromString("58e0a7d7-eebc-11d8-9669-0800200c9a66");
    
    private final UUID PRODUCT_ID = UUID.fromString("f8c3de3d-1fea-4d7c-a8b0-29f63c4c3454");
    
    private final UUID ORDER_ID = UUID.fromString("229A6A53-B428-4FFB-A835-E8F36B5B4B1E");
    
    private final UUID SAGA_ID = UUID.fromString("15a497c1-0f4b-4eff-b9f4-c402c8c07afa");
    
    private final BigDecimal PRICE = new BigDecimal("250.00");
    
    @BeforeAll
    public void init() {
        createOrderCommand = CreateOrderCommand.builder()
                                               .customerId(CUSTOMER_ID)
                                               .restaurantId(RESTAURANT_ID)
                                               .orderAddress(OrderAddress.builder()
                                                                         .street("street_1")
                                                                         .postalCode("11236")
                                                                         .city("Athens")
                                                                         .build())
                                               .price(PRICE)
                                               .orderItems(List.of(OrderItems.builder()
                                                                             .productId(PRODUCT_ID)
                                                                             .price(new BigDecimal("50.00"))
                                                                             .quantity(3)
                                                                             .subTotal(new BigDecimal("150.00"))
                                                                             .build(),
                                                                   OrderItems.builder()
                                                                             .productId(PRODUCT_ID)
                                                                             .price(new BigDecimal("50.00"))
                                                                             .quantity(2)
                                                                             .subTotal(new BigDecimal("100.00"))
                                                                             .build()))
                                               .build();
        
        createOrderCommandWrongPrice = CreateOrderCommand.builder()
                                                         .customerId(CUSTOMER_ID)
                                                         .restaurantId(RESTAURANT_ID)
                                                         .orderAddress(OrderAddress.builder()
                                                                                   .street("street_1")
                                                                                   .postalCode("11236")
                                                                                   .city("Athens")
                                                                                   .build())
                                                         .price(BigDecimal.valueOf(300))
                                                         .orderItems(List.of(OrderItems.builder()
                                                                                       .productId(PRODUCT_ID)
                                                                                       .price(new BigDecimal("50.00"))
                                                                                       .quantity(3)
                                                                                       .subTotal(new BigDecimal("150.00"))
                                                                                       .build(),
                                                                             OrderItems.builder()
                                                                                       .productId(PRODUCT_ID)
                                                                                       .price(new BigDecimal("50.00"))
                                                                                       .quantity(2)
                                                                                       .subTotal(new BigDecimal("100.00"))
                                                                                       .build()))
                                                         .build();
        
        createOrderCommandWrongProductPrice = CreateOrderCommand.builder()
                                                                .customerId(CUSTOMER_ID)
                                                                .restaurantId(RESTAURANT_ID)
                                                                .orderAddress(OrderAddress.builder()
                                                                                          .street("street_1")
                                                                                          .postalCode("11236")
                                                                                          .city("Athens")
                                                                                          .build())
                                                                .price(BigDecimal.valueOf(220))
                                                                .orderItems(List.of(OrderItems.builder()
                                                                                              .productId(PRODUCT_ID)
                                                                                              .price(new BigDecimal("40.00"))
                                                                                              .quantity(3)
                                                                                              .subTotal(new BigDecimal("120.00"))
                                                                                              .build(),
                                                                                    OrderItems.builder()
                                                                                              .productId(PRODUCT_ID)
                                                                                              .price(new BigDecimal("50.00"))
                                                                                              .quantity(2)
                                                                                              .subTotal(new BigDecimal("100.00"))
                                                                                              .build()))
                                                                .build();
        
        Customer customer = new Customer(new CustomerId(CUSTOMER_ID));
        
        Restaurant restaurantResponse = Restaurant.builder()
                                                  .restaurantId(new RestaurantId(createOrderCommand.getRestaurantId()))
                                                  .active(true)
                                                  .products(List.of(new Product(new ProductId(PRODUCT_ID), "name-1", new Money(new BigDecimal("50.00"))),
                                                                    new Product(new ProductId(PRODUCT_ID), "name-2", new Money(new BigDecimal("50.00")))))
                                                  .build();
        
        Order order = orderDataMapper.createOrderCommandToOrder(createOrderCommand);
        order.setId(new OrderId(UUID.randomUUID()));
        
        when(customerRepository.findCustomer(CUSTOMER_ID))
            .thenReturn(Optional.of(customer));
        
        when(restaurantRepository.findRestaurantInformation(orderDataMapper.createOrderCommandToRestaurant(createOrderCommand)))
            .thenReturn(Optional.of(restaurantResponse));
        
        when(orderRepository.save(any(Order.class)))
            .thenReturn(order);
        
        when(paymentOutboxRepository.save(any(OrderPaymentOutboxMessage.class))).thenReturn(getOrderPaymentOutboxMessage());
        
    }
    
    @Test
    void testCreateOrder() {
        CreateOrderResponse createOrderResponse = orderApplicationService.createOrder(createOrderCommand);
        assertEquals(PENDING, createOrderResponse.getOrderStatus());
        assertNotNull(createOrderResponse.getOrderTrackingId());
    }
    
    @Test
    void testCreateOrderWithWrongTotalPrice() {
        assertThrows(OrderDomainException.class, () -> orderApplicationService.createOrder(createOrderCommandWrongPrice));
    }
    
    private OrderPaymentOutboxMessage getOrderPaymentOutboxMessage() {
        OrderPaymentEventPayload orderPaymentEventPayload = OrderPaymentEventPayload.builder()
                                                                                    .orderId(ORDER_ID.toString())
                                                                                    .customerId(CUSTOMER_ID.toString())
                                                                                    .price(PRICE)
                                                                                    .createdAt(ZonedDateTime.now())
                                                                                    .paymentOrderStatus(PaymentOrderStatus.PENDING.name())
                                                                                    .build();
        
        return OrderPaymentOutboxMessage.builder()
                                        .id(UUID.randomUUID())
                                        .sagaId(SAGA_ID)
                                        .createdAt(ZonedDateTime.now())
                                        .type(ORDER_SAGA_NAME)
                                        .payload(createPayload(orderPaymentEventPayload))
                                        .orderStatus(OrderStatus.PENDING)
                                        .sagaStatus(SagaStatus.STARTED)
                                        .outboxStatus(OutboxStatus.STARTED)
                                        .version(0)
                                        .build();
    }
    
    private String createPayload(OrderPaymentEventPayload orderPaymentEventPayload) {
        try {
            return objectMapper.writeValueAsString(orderPaymentEventPayload);
        } catch (JsonProcessingException e) {
            throw new OrderDomainException("Cannot create OrderPaymentEventPayload object!");
        }
    }
    
}
