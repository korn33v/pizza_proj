package com.example.pizza.application.port.in.result;

import java.util.UUID;

// [SEMINAR-33] Специальный результат для семинара: помогает показать студентам,
// как команда превращается в событие и как policy подсказывает следующий шаг.
public record WorkflowStepResult(
    UUID orderId,
    String executedCommand,
    String emittedEvent,
    OrderResult order,
    OrderBoardReadModel boardReadModel,
    PolicyHintResult policyHint
) {}
