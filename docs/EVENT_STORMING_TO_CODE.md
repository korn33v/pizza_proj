# Event Storming → Code в Pizza Delivery

## Упрощённая цепочка

```text
CreateOrder      -> OrderCreated      -> Policy: ReceivePayment
ReceivePayment   -> PaymentReceived   -> Policy: StartCooking
StartCooking     -> CookingStarted    -> Policy: AssignCourier
AssignCourier    -> CourierAssigned   -> Policy: DeliverOrder
DeliverOrder     -> OrderDelivered
```

## Где это в коде

- **Commands**: `application/port/in/command/*`
- **Aggregate**: `domain/model/Order.java`
- **Domain Events**: `domain/event/*`
- **Policies**: `application/service/policy/*`
- **Read Model**: `application/port/in/result/OrderBoardReadModel.java`
- **Seminar API**: `adapter/in/web/SeminarWorkflowController.java`

## Как читать код на семинаре

1. `SeminarWorkflowController` — входная команда из внешнего мира.
2. `CreateOrderWorkflowInteractor` / `ReceivePaymentInteractor` / ... — use case на уровне application.
3. `Order` — агрегат, который защищает допустимые переходы состояния.
4. `OrderCreatedEvent`, `PaymentReceivedEvent`, ... — явные доменные события.
5. `PaymentRequestedPolicy`, `KitchenStartPolicy`, ... — реакции системы на событие.
6. `OrderBoardReadModel` — отдельная модель для чтения, не равная агрегату.
