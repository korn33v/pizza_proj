package com.example.pizza.application.service.policy;

import com.example.pizza.application.port.in.result.PolicyHintResult;
import com.example.pizza.domain.event.PaymentReceivedEvent;

public final class KitchenStartPolicy {
  public PolicyHintResult react(PaymentReceivedEvent event) {
    return new PolicyHintResult(
        "KitchenStartPolicy",
        "StartCooking",
        "После оплаты заказ можно передавать на кухню."
    );
  }
}
