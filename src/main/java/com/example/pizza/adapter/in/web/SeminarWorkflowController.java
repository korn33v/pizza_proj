package com.example.pizza.adapter.in.web;

import com.example.pizza.adapter.in.web.dto.CreateOrderRequest;
import com.example.pizza.adapter.in.web.dto.PizzaItemDto;
import com.example.pizza.adapter.in.web.dto.WorkflowStepResponse;
import com.example.pizza.application.port.in.AssignCourierUseCase;
import com.example.pizza.application.port.in.CreateOrderWorkflowUseCase;
import com.example.pizza.application.port.in.DeliverOrderUseCase;
import com.example.pizza.application.port.in.GetOrderBoardUseCase;
import com.example.pizza.application.port.in.ReceivePaymentUseCase;
import com.example.pizza.application.port.in.StartCookingUseCase;
import com.example.pizza.application.port.in.command.AssignCourierCommand;
import com.example.pizza.application.port.in.command.CreateOrderCommand;
import com.example.pizza.application.port.in.command.DeliverOrderCommand;
import com.example.pizza.application.port.in.command.GetOrderBoardQuery;
import com.example.pizza.application.port.in.command.ReceivePaymentCommand;
import com.example.pizza.application.port.in.command.StartCookingCommand;
import com.example.pizza.application.port.in.result.OrderBoardReadModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

// [SEMINAR-35] Отдельный контроллер для преподавательского демо.
// Он не заменяет обычный CRUD API, а даёт наглядный walkthrough по шагам Event Storming.
@RestController
@RequestMapping("/seminar/workflow/orders")
public class SeminarWorkflowController {

  private final CreateOrderWorkflowUseCase createOrderWorkflowUseCase;
  private final ReceivePaymentUseCase receivePaymentUseCase;
  private final StartCookingUseCase startCookingUseCase;
  private final AssignCourierUseCase assignCourierUseCase;
  private final DeliverOrderUseCase deliverOrderUseCase;
  private final GetOrderBoardUseCase getOrderBoardUseCase;

  public SeminarWorkflowController(
      CreateOrderWorkflowUseCase createOrderWorkflowUseCase,
      ReceivePaymentUseCase receivePaymentUseCase,
      StartCookingUseCase startCookingUseCase,
      AssignCourierUseCase assignCourierUseCase,
      DeliverOrderUseCase deliverOrderUseCase,
      GetOrderBoardUseCase getOrderBoardUseCase
  ) {
    this.createOrderWorkflowUseCase = createOrderWorkflowUseCase;
    this.receivePaymentUseCase = receivePaymentUseCase;
    this.startCookingUseCase = startCookingUseCase;
    this.assignCourierUseCase = assignCourierUseCase;
    this.deliverOrderUseCase = deliverOrderUseCase;
    this.getOrderBoardUseCase = getOrderBoardUseCase;
  }

  @PostMapping
  public ResponseEntity<WorkflowStepResponse> create(@RequestBody CreateOrderRequest request) {
    var result = createOrderWorkflowUseCase.create(toCommand(request));
    return ResponseEntity.ok(WorkflowStepResponse.fromResult(result));
  }

  @PostMapping("/{id}/payment-received")
  public ResponseEntity<WorkflowStepResponse> receivePayment(@PathVariable UUID id) {
    var result = receivePaymentUseCase.receive(new ReceivePaymentCommand(id));
    return ResponseEntity.ok(WorkflowStepResponse.fromResult(result));
  }

  @PostMapping("/{id}/start-cooking")
  public ResponseEntity<WorkflowStepResponse> startCooking(@PathVariable UUID id) {
    var result = startCookingUseCase.start(new StartCookingCommand(id));
    return ResponseEntity.ok(WorkflowStepResponse.fromResult(result));
  }

  @PostMapping("/{id}/assign-courier")
  public ResponseEntity<WorkflowStepResponse> assignCourier(@PathVariable UUID id) {
    var result = assignCourierUseCase.assign(new AssignCourierCommand(id));
    return ResponseEntity.ok(WorkflowStepResponse.fromResult(result));
  }

  @PostMapping("/{id}/deliver")
  public ResponseEntity<WorkflowStepResponse> deliver(@PathVariable UUID id) {
    var result = deliverOrderUseCase.deliver(new DeliverOrderCommand(id));
    return ResponseEntity.ok(WorkflowStepResponse.fromResult(result));
  }

  @GetMapping("/{id}/board")
  public ResponseEntity<WorkflowStepResponse.BoardViewResponse> board(@PathVariable UUID id) {
    OrderBoardReadModel model = getOrderBoardUseCase.get(new GetOrderBoardQuery(id));
    return ResponseEntity.ok(WorkflowStepResponse.BoardViewResponse.fromReadModel(model));
  }

  private static CreateOrderCommand toCommand(CreateOrderRequest request) {
    if (request == null) {
      throw new IllegalArgumentException("request must be provided");
    }
    CreateOrderCommand.AddressCommand address = new CreateOrderCommand.AddressCommand(
        request.address() == null ? null : request.address().city(),
        request.address() == null ? null : request.address().street(),
        request.address() == null ? null : request.address().house(),
        request.address() == null ? null : request.address().apartment(),
        request.address() == null ? null : request.address().postcode()
    );

    List<CreateOrderCommand.PizzaItemCommand> items = (request.items() == null ? List.<PizzaItemDto>of() : request.items())
        .stream()
        .map(i -> new CreateOrderCommand.PizzaItemCommand(
            i == null ? null : i.name(),
            i == null ? null : i.size(),
            i == null ? 0 : i.qty()
        ))
        .toList();

    return new CreateOrderCommand(address, items);
  }
}
