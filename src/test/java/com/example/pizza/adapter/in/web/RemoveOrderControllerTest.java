package com.example.pizza.adapter.in.web;

import com.example.pizza.application.port.in.RemoveOrderUseCase;
import com.example.pizza.application.port.in.command.CancelOrderCommand;
import com.example.pizza.application.service.exception.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.UUID;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RemoveOrderControllerTest {
  private final RemoveOrderUseCase remove = mock(RemoveOrderUseCase.class);
  private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new RemoveOrderController(remove))
      .setControllerAdvice(new WebExceptionHandler()).build();
  private final UUID id = UUID.randomUUID();

  @Test
  void passesParametersAndReturnsNoContent() throws Exception {
    mvc.perform(delete("/orders/{id}/remove", id).param("reason", "Mistake"))
        .andExpect(status().isNoContent()).andExpect(content().string(""));
    verify(remove).remove(new CancelOrderCommand(id, "Mistake"));
  }

  @Test
  void rejectsMissingReasonAndMalformedId() throws Exception {
    mvc.perform(delete("/orders/{id}/remove", id)).andExpect(status().isBadRequest());
    mvc.perform(delete("/orders/invalid/remove").param("reason", "Mistake"))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(remove);
  }

  @Test
  void mapsApplicationErrors() throws Exception {
    doThrow(new ValidationException("reason")).when(remove).remove(any());
    mvc.perform(delete("/orders/{id}/remove", id).param("reason", " "))
        .andExpect(status().isBadRequest());
    doThrow(new NotFoundException("missing")).when(remove).remove(any());
    mvc.perform(delete("/orders/{id}/remove", id).param("reason", "Mistake"))
        .andExpect(status().isNotFound());
    doThrow(new BusinessRuleViolationException("status")).when(remove).remove(any());
    mvc.perform(delete("/orders/{id}/remove", id).param("reason", "Mistake"))
        .andExpect(status().isConflict());
  }
}
