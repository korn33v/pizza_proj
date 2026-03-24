package com.example.pizza.adapter.in.web.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
    UUID id,
    AddressDto address,
    List<PizzaItemDto> items,
    String status,
    BigDecimal totalAmount,
    String currency,
    String deliveryZone
) {}
