package com.example.pizza.application.port.in.command;

import java.util.UUID;

public record DeliverOrderCommand(UUID orderId) {}
