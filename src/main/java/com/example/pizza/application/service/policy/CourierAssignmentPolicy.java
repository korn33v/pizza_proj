package com.example.pizza.application.service.policy;

import com.example.pizza.application.port.in.result.PolicyHintResult;
import com.example.pizza.domain.event.CookingStartedEvent;

public final class CourierAssignmentPolicy {
  public PolicyHintResult react(CookingStartedEvent event) {
    return new PolicyHintResult(
        "CourierAssignmentPolicy",
        "AssignCourier",
        "Когда кухня начала готовить заказ, можно готовить доставку и назначать курьера."
    );
  }
}
