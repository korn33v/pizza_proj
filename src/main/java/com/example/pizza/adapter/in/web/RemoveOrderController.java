package com.example.pizza.adapter.in.web;

import com.example.pizza.application.port.in.RemoveOrderUseCase;
import com.example.pizza.application.port.in.command.CancelOrderCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class RemoveOrderController {
  private final RemoveOrderUseCase removeOrderUseCase;

  public RemoveOrderController(RemoveOrderUseCase removeOrderUseCase) {
    this.removeOrderUseCase = removeOrderUseCase;
  }

  @Operation(summary = "Полностью удалить ошибочный заказ",
      description = "Необратимо удаляет заказ, позиции и сохранённую причину из БД. "
          + "Правила аналогичны отмене: CREATED, PAID, COOKING либо CANCELLED с той же причиной. "
          + "Переданная причина проверяется, но после удаления не хранится.")
  @ApiResponse(responseCode = "204", description = "Заказ и позиции удалены")
  @ApiResponse(responseCode = "400", description = "Некорректный UUID или причина (от 1 до 1000 символов)")
  @ApiResponse(responseCode = "404", description = "Заказ не найден, в том числе при повторном удалении")
  @ApiResponse(responseCode = "409", description = "Удаление запрещено по правилам отмены")
  @DeleteMapping("/{orderId}/remove")
  public ResponseEntity<Void> remove(
      @Parameter(description = "Номер заказа (UUID)", required = true)
      @PathVariable("orderId") UUID orderId,
      @Parameter(description = "Причина удаления, от 1 до 1000 символов", required = true)
      @RequestParam("reason") String reason) {
    removeOrderUseCase.remove(new CancelOrderCommand(orderId, reason));
    return ResponseEntity.noContent().build();
  }
}
