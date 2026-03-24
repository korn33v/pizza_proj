package com.example.pizza.application.service.policy;

import com.example.pizza.application.port.in.result.PolicyHintResult;
import com.example.pizza.domain.event.CourierAssignedEvent;

public final class DeliveryCompletionPolicy {
  public PolicyHintResult react(CourierAssignedEvent event) {
    return new PolicyHintResult(
        "DeliveryCompletionPolicy",
        "DeliverOrder",
        "После назначения курьера следующий ожидаемый шаг — закрыть доставку."
    );
  }
}
