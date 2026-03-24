package com.example.pizza.application.service.policy;

import com.example.pizza.application.port.in.result.PolicyHintResult;
import com.example.pizza.domain.event.OrderCreatedEvent;

public final class PaymentRequestedPolicy {
  public PolicyHintResult react(OrderCreatedEvent event) {
    return new PolicyHintResult(
        "PaymentRequestedPolicy",
        "ReceivePayment",
        "После создания заказа система ожидает подтверждение оплаты."
    );
  }
}
