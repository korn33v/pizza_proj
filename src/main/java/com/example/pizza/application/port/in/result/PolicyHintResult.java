package com.example.pizza.application.port.in.result;

// [SEMINAR-31] Policy = не просто if в коде, а отдельное бизнес-решение:
// когда произошло событие X, системе нужно подсказать/инициировать следующее действие.
public record PolicyHintResult(
    String policyName,
    String suggestedNextCommand,
    String explanation
) {}
