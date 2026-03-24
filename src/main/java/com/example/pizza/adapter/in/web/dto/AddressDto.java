package com.example.pizza.adapter.in.web.dto;

public record AddressDto(
    String city,
    String street,
    String house,
    String apartment,
    String postcode
) {}
