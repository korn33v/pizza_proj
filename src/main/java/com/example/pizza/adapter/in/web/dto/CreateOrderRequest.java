package com.example.pizza.adapter.in.web.dto;

import java.util.List;

public record CreateOrderRequest(
    AddressDto address,
    List<PizzaItemDto> items
) {}
