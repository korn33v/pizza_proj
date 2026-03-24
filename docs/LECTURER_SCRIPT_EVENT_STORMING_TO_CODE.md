# Сценарий рассказа преподавателя: от Event Storming к коду

## Цель показа
Провести студентов по цепочке:
**command → aggregate → domain event → policy → read model**.

---

## 1. Показываем доску Event Storming

Реплика преподавателя:
> На доске мы выделили команды, события, политики и read model. Теперь посмотрим, как эта доска превращается в код приложения.

Показать цепочку:
- `CreateOrder` → `OrderCreated`
- `ReceivePayment` → `PaymentReceived`
- `StartCooking` → `CookingStarted`
- `AssignCourier` → `CourierAssigned`
- `DeliverOrder` → `OrderDelivered`

Переход:
> В коде каждая синяя команда станет входным use case, а каждый оранжевый стикер — отдельным доменным событием.

---

## 2. Показываем вход в систему: controller + command

Открыть:
- `adapter/in/web/SeminarWorkflowController.java`
- `application/port/in/command/CreateOrderCommand.java`

Реплика преподавателя:
> Команда из Event Storming — это не доменная сущность и не HTTP DTO. Это контракт входа в application слой.

Подчеркнуть:
- контроллер тонкий
- он только собирает command и вызывает use case

Вопрос студентам:
> Почему command живёт в application, а не в domain?

Ответ:
> Потому что это граница use case, а не бизнес-сущность.

---

## 3. Показываем aggregate

Открыть:
- `domain/model/Order.java`

Реплика преподавателя:
> Aggregate — это граница согласованности. Через него мы меняем состояние заказа так, чтобы не нарушить бизнес-правила.

Показать методы:
- `markPaid(...)`
- `startCooking(...)`
- `assignCourier(...)`
- `deliver(...)`

Подчеркнуть:
> Это не просто data holder. Агрегат содержит поведение.

Вопрос студентам:
> Почему эти методы лучше, чем прямой `setStatus(...)`?

---

## 4. Показываем доменные события

Открыть:
- `domain/event/OrderCreatedEvent.java`
- `domain/event/PaymentReceivedEvent.java`

Реплика преподавателя:
> Оранжевый стикер на доске — это факт, который уже произошёл. В коде мы также делаем этот факт отдельным объектом.

Подчеркнуть:
- событие формулируется в прошедшем времени
- событие возникает после успешного изменения агрегата

---

## 5. Показываем use case и политику на одном шаге

Открыть:
- `application/service/ReceivePaymentInteractor.java`
- `application/service/policy/KitchenStartPolicy.java`

Реплика преподавателя:
> Здесь видно полную цепочку. Команда `ReceivePayment` приходит в use case, use case загружает агрегат, агрегат меняет состояние, после этого рождается событие `PaymentReceived`, а policy подсказывает следующий шаг — `StartCooking`.

Подчеркнуть по строкам:
1. загрузили `Order`
2. вызвали `existing.markPaid(...)`
3. сохранили новый `Order`
4. создали `PaymentReceivedEvent`
5. вызвали `kitchenStartPolicy.react(event)`

Вопрос студентам:
> Почему policy лучше выделить в отдельный класс, а не спрятать внутрь controller?

---

## 6. Показываем read model

Открыть:
- `application/port/in/result/OrderBoardReadModel.java`
- `GET /seminar/workflow/orders/{id}/board`

Реплика преподавателя:
> Read Model не защищает инварианты и не управляет процессом. Она нужна, чтобы удобно показать пользователю или преподавателю, где находится заказ в workflow.

Подчеркнуть:
- aggregate = для изменения состояния
- read model = для чтения

Вопрос студентам:
> Почему не стоит отдавать aggregate напрямую в UI и называть это read model?

---

## 7. Живой сценарий через curl

### 7.1 Создаём заказ
```bash
curl -s -X POST http://localhost:8080/seminar/workflow/orders   -H 'Content-Type: application/json'   -d '{
    "address": {"city":"Amsterdam","street":"Damrak","house":"1","apartment":"12","postcode":"1012"},
    "items": [
      {"name":"Margherita","size":"MEDIUM","qty":2},
      {"name":"Pepperoni","size":"LARGE","qty":1}
    ]
  }'
```

Реплика преподавателя:
> Смотрите: response сразу показывает команду `CreateOrder`, событие `OrderCreated`, policy `PaymentRequestedPolicy` и следующий ожидаемый шаг `ReceivePayment`.

### 7.2 Принимаем оплату
```bash
curl -s -X POST http://localhost:8080/seminar/workflow/orders/<ID>/payment-received
```

Реплика преподавателя:
> Здесь студенты видят, что после `PaymentReceived` policy действительно предлагает `StartCooking`.

### 7.3 Запускаем кухню
```bash
curl -s -X POST http://localhost:8080/seminar/workflow/orders/<ID>/start-cooking
```

### 7.4 Назначаем курьера
```bash
curl -s -X POST http://localhost:8080/seminar/workflow/orders/<ID>/assign-courier
```

### 7.5 Закрываем доставку
```bash
curl -s -X POST http://localhost:8080/seminar/workflow/orders/<ID>/deliver
```

### 7.6 Смотрим board view
```bash
curl -s http://localhost:8080/seminar/workflow/orders/<ID>/board
```

Реплика преподавателя:
> Это уже зелёный стикер read model. Он нужен для чтения состояния процесса, а не для изменения бизнес-логики.

---

## 8. Финальный вывод для студентов

> Event Storming — это не просто рисунок на доске. Его элементы почти напрямую переводятся в структуру кода:
>
> - command → use case
> - aggregate → доменная модель с поведением
> - domain event → отдельный факт предметной области
> - policy → реакция системы на событие
> - read model → удобное представление для чтения
