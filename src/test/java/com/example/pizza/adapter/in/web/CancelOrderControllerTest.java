package com.example.pizza.adapter.in.web;

import com.example.pizza.application.port.in.*;
import com.example.pizza.application.port.in.command.CancelOrderCommand;
import com.example.pizza.application.service.exception.BusinessRuleViolationException;
import com.example.pizza.application.service.exception.NotFoundException;
import com.example.pizza.application.service.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CancelOrderControllerTest {
  private final CancelOrderUseCase cancel = mock(CancelOrderUseCase.class);
  private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new OrderController(
      mock(CreateOrderUseCase.class), mock(ChangeOrderStatusUseCase.class),
      mock(GetOrderUseCase.class), mock(ListOrdersUseCase.class), cancel))
      .setControllerAdvice(new WebExceptionHandler()).build();
  private final UUID id = UUID.randomUUID();

  @Test
  void deletePassesBothParametersAndReturnsNoContent() throws Exception {
    mvc.perform(delete("/orders/{orderId}", id).param("reason", "Ошибочный заказ"))
        .andExpect(status().isNoContent()).andExpect(content().string(""));
    verify(cancel).cancel(new CancelOrderCommand(id, "Ошибочный заказ"));
  }

  @Test
  void missingReasonAndInvalidUuidAreBadRequests() throws Exception {
    mvc.perform(delete("/orders/{orderId}", id)).andExpect(status().isBadRequest());
    mvc.perform(delete("/orders/not-a-uuid").param("reason", "Mistake")).andExpect(status().isBadRequest());
    verifyNoInteractions(cancel);
  }

  @Test
  void validationFailureIsBadRequest() throws Exception {
    doThrow(new ValidationException("Invalid reason")).when(cancel).cancel(any());
    mvc.perform(delete("/orders/{orderId}", id).param("reason", " "))
        .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
  }

  @Test
  void missingOrderIsNotFound() throws Exception {
    doThrow(new NotFoundException("Order not found")).when(cancel).cancel(any());
    mvc.perform(delete("/orders/{orderId}", id).param("reason", "Mistake"))
        .andExpect(status().isNotFound());
  }

  @Test
  void forbiddenCancellationIsConflict() throws Exception {
    doThrow(new BusinessRuleViolationException("Too late to cancel")).when(cancel).cancel(any());
    mvc.perform(delete("/orders/{orderId}", id).param("reason", "Mistake"))
        .andExpect(status().isConflict());
  }
}
