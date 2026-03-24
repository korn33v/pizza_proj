# Pizza Delivery — учебный проект (DDD + Onion/Clean + Ports & Adapters)

**Цель:** показать границы слоёв, направление зависимостей, порты/адаптеры и тестирование use case без Spring.

---

## Architectural Kata — артефакты

### User Stories (3–4)
1) **Как клиент**, я хочу **создать заказ** (пиццы + адрес), чтобы система посчитала цену и приняла заказ.  
2) **Как кассир**, я хочу **менять статус заказа** (CREATED → PAID → COOKING → OUT_FOR_DELIVERY → DELIVERED), чтобы отражать прогресс доставки.  
3) **Как курьер/поддержка**, я хочу **посмотреть заказ по id**, чтобы понимать что везти/что произошло.  
4) **Как менеджер**, я хочу **запретить нелепые переходы статуса**, чтобы не портили данные (например, DELIVERED → PAID).

### NFR (Non-Functional Requirements)
- **Независимость от фреймворков:** application слой без Spring/JPA (см. `application/service/*`) // [SEMINAR-03]
- **Тестируемость:** use case тестируются моками портов без Spring Test // [SEMINAR-15]
- **Заменяемость БД/внешних сервисов:** persistence/external — это адаптеры, подменяются wiring-конфигом // [SEMINAR-11], [SEMINAR-16]

### Quality Attribute Scenarios (3)
1) **Testability scenario:**  
   *Given* разработчик меняет правила статусов, *When* запускает `./mvnw test`, *Then* unit-тесты use case падают/проходят без поднятия БД и Spring.
2) **Replaceability scenario (DB):**  
   *Given* нужно перейти с PostgreSQL на другую БД, *When* переписывается `adapter/out/persistence`, *Then* `application/service` не меняется.
3) **Replaceability scenario (External):**  
   *Given* вместо стаба нужен реальный сервис зон доставки, *When* добавляется новый адаптер реализации `DeliveryZonePort`, *Then* use case не меняется — меняется только wiring.

---

## ASCII диаграммы

### Onion / Clean — слои и зависимости
```
          +-------------------------------+
          |        adapter/in (web)       |
          |  controllers, HTTP DTO        |
          +---------------+---------------+
                          |
                          v  depends on
          +-------------------------------+
          |          application          |
          | ports(in/out), use cases     |
          |  (NO Spring/JPA annotations) |
          +---------------+---------------+
                          |
                          v  depends on
          +-------------------------------+
          |             domain            |
          | model, policies, invariants   |
          +-------------------------------+

  adapter/out (persistence, external) находится "снаружи" и зависит от application/domain.
  infrastructure/config связывает всё в граф (wiring).
```

### C4 L1 — Context
```
+---------+        HTTP        +-----------------------+      JDBC      +-----------+
| Client  |  <-------------->  | Pizza Delivery System |  <---------->  | Postgres  |
+---------+                     +----------+------------+                +-----------+
                                            |
                                            | (port)
                                            v
                                   +------------------+
                                   | Delivery Zone     |
                                   | Service (stub)    |
                                   +------------------+
```

### Компоненты (упрощённо)
```
[Web Controller]
      |
      v
(CreateOrderUseCase)  (ChangeOrderStatusUseCase)  (GetOrderUseCase)
      |                         |                         |
      v                         v                         v
  CreateOrderInteractor   ChangeOrderStatusInteractor   GetOrderInteractor
      |                         |
      +-----------+-------------+
                  |
        +---------+---------+
        |                   |
        v                   v
(OrderRepositoryPort)   (DeliveryZonePort)
        |                   |
        v                   v
[JPA Adapter]         [Stub External Adapter]
```

---

## Правила зависимостей (важно)
- `domain` **не знает** про `application`, `adapter`, `infrastructure`.
- `application` знает про `domain`, но **не знает** про Spring/JPA/HTTP. // [SEMINAR-03]
- `adapter/*` может зависеть от `application` и `domain`.
- `infrastructure/config` зависит от всех и **собирает** зависимости. // [SEMINAR-16]

---

## Запуск

### 1) Тесты (unit без Spring)
```bash
./mvnw test
```

### 2) Полный запуск (PostgreSQL + app)
```bash
docker compose up --build
```
Приложение будет на `http://localhost:8080`.

---

## Curl-демо

### Создать заказ
```bash
curl -i -X POST http://localhost:8080/orders   -H 'Content-Type: application/json'   -d '{
    "address": {"city":"Amsterdam","street":"Damrak","house":"1","apartment":"12","postcode":"1012"},
    "items": [
      {"name":"Margherita","size":"MEDIUM","qty":2},
      {"name":"Pepperoni","size":"LARGE","qty":1}
    ]
  }'
```

### Получить заказ
```bash
curl -s http://localhost:8080/orders/<ID>
```

### Сменить статус
```bash
curl -i -X PATCH http://localhost:8080/orders/<ID>/status   -H 'Content-Type: application/json'   -d '{"status":"PAID"}'
```

---

## Экскурсия по проекту (10–12 шагов с практической проверкой)

1) `PizzaDeliveryApplication.java` — границы слоёв и направление зависимостей. // [SEMINAR-01]  
   Проверка: `./mvnw test` (пока просто убеждаемся, что собирается).

2) `domain/model/Order.java` — иммутабельный домен + инварианты. // [SEMINAR-04]  
   Проверка: объект нельзя “дозаполнить” сеттерами.

3) `domain/policy/OrderPricingPolicy.java` — правило расчёта цены. // [SEMINAR-08]  
   Проверка: найдите цены SMALL/MEDIUM/LARGE.

4) `domain/policy/OrderStatusTransitionPolicy.java` — допустимые переходы статусов. // [SEMINAR-09]  
   Проверка: найдите запрет DELIVERED → что угодно.

5) `application/port/in/CreateOrderUseCase.java` — inbound порт (DIP). // [SEMINAR-02]  
   Проверка: это интерфейс и он не знает про Spring.

6) `application/service/CreateOrderInteractor.java` — application без Spring. // [SEMINAR-03]  
   Проверка: найдите отсутствие `@Service` и вызов портов.

7) `application/port/out/DeliveryZonePort.java` — внешний сервис через порт. // [SEMINAR-11]  
   Проверка: “реальный HTTP” добавляется через адаптер.

8) `adapter/in/web/OrderController.java` — тонкий контроллер. // [SEMINAR-10]  
   Проверка: бизнес-логики в контроллере ~0.

9) `adapter/out/persistence/entity/OrderEntity.java` — “JPA Entity != доменная сущность”. // [SEMINAR-06]  
   Проверка: сравните с `domain/model/Order.java`.

10) `adapter/out/persistence/mapper/OrderMapper.java` — где происходит маппинг. // [SEMINAR-07], [SEMINAR-14]  
    Проверка: найдите антипаттерн “entity наружу”.

11) `infrastructure/config/ApplicationWiringConfig.java` — где wiring и почему. // [SEMINAR-16]  
    Проверка: найдите `@Bean` для interactors и adapters.

12) End-to-end:  
    Команда: `docker compose up --build`  
    Затем: `POST /orders`, взять `id`, `GET /orders/{id}`, `PATCH /orders/{id}/status`.  
    Ожидаемо: статусы меняются по правилам (нарушение бизнес правила даст 409).

---

## Учебная навигация по якорям (SEMINAR markers)

- [SEMINAR-01] `PizzaDeliveryApplication.java` — направление зависимостей и границы слоёв
- [SEMINAR-02] `application/port/in/CreateOrderUseCase.java` — DIP через inbound порт
- [SEMINAR-03] `application/service/CreateOrderInteractor.java` — application без Spring
- [SEMINAR-04] `domain/model/Order.java` — иммутабельность и инварианты
- [SEMINAR-05] `application/port/in/result/OrderResult.java` — use case result vs domain vs HTTP
- [SEMINAR-06] `adapter/out/persistence/entity/OrderEntity.java` — JPA entity != domain
- [SEMINAR-07] `adapter/out/persistence/mapper/OrderMapper.java` — явный маппинг
- [SEMINAR-08] `domain/policy/OrderPricingPolicy.java` — бизнес-правила цены
- [SEMINAR-09] `domain/policy/OrderStatusTransitionPolicy.java` — переходы статусов как правило
- [SEMINAR-10] `adapter/in/web/OrderController.java` — тонкий контроллер
- [SEMINAR-11] `application/port/out/DeliveryZonePort.java` — замена внешней интеграции без правок use case
- [SEMINAR-12] `domain/model/PizzaSize.java` — ubiquitous language (enum)
- [SEMINAR-13] `application/service/CreateOrderInteractor.java` — ANTIPATTERN: use case зависит от Spring
- [SEMINAR-14] `adapter/out/persistence/mapper/OrderMapper.java` — ANTIPATTERN: наружу утекают entity
- [SEMINAR-15] `CreateOrderInteractorTest.java` — мокаем порты, а не JPA
- [SEMINAR-16] `ApplicationWiringConfig.java` — wiring в infrastructure
- [SEMINAR-17] `V1__init.sql` — миграции Flyway как дисциплина
- [SEMINAR-18] `OrderPersistenceAdapter.java` — транзакции во внешнем слое
- [SEMINAR-19] `mvnw` / `mvnw.cmd` — воспроизводимость сборки
- [SEMINAR-20] `OrderController.java` — ANTIPATTERN: жирный контроллер

---

## Антипаттерны и как их распознать (минимум 6)

1) **Домен с JPA-аннотациями**  
   Признак: `@Entity` в `domain/*`. Итог: domain начинает зависеть от ORM, теряет чистоту.

2) **Жирный контроллер** // [SEMINAR-20]  
   Признак: в контроллере есть расчёт цены, переходы статусов, вызовы репозитория.

3) **Use case дергает Spring/HTTP/JPA напрямую** // [SEMINAR-13]  
   Признак: `RestTemplate/WebClient/JpaRepository` в `application/service`.

4) **Репозиторий возвращает JPA Entity наружу** // [SEMINAR-14]  
   Признак: контроллер/интерактор возвращает `OrderEntity`, ловите LAZY и утечки ORM.

5) **@Transactional в application**  
   Признак: `@Transactional` на interactor. Итог: Spring протекает в application.

6) **“Сквозные DTO” (один класс на всё)**  
   Признак: один `OrderDto` используется как HTTP request/response, домен и entity.

7) **Бизнес-правила размазаны по слоям**  
   Признак: часть правил статусов в контроллере, часть в сервисе, часть в entity.

---

## Чеклист готовности (10 пунктов)

1) Application слой без аннотаций Spring/JPA.  
2) Domain слой не содержит DTO/Entity/Spring-классов.  
3) Все use cases вызываются через inbound порт(ы).  
4) Все внешние зависимости use case — через outbound порт(ы).  
5) Маппинг domain<->entity лежит в adapter/out/persistence.  
6) Контроллеры тонкие.  
7) Есть миграции Flyway, `ddl-auto=validate`.  
8) Есть минимум 2 unit-теста use case с Mockito без Spring.  
9) Wiring собран в `infrastructure/config`.  
10) Антипаттерны из списка не встречаются или помечены как учебные.

---

## Домашнее задание (следующий шаг)

Добавить модуль уведомлений (SMS/Email) при смене статуса заказа.

- // [SEMINAR-HW-01] DIP: создать outbound порт NotificationPort в application и использовать его в ChangeOrderStatusInteractor.
- // [SEMINAR-HW-02] Adapter: реализовать два адаптера (EmailStubAdapter, SmsStubAdapter) во внешнем слое.
- // [SEMINAR-HW-03] Wiring: переключение реализации через ApplicationWiringConfig без правок use case.


---

## Web UI (React + MUI)


**Важно:** браузер не резолвит `http://app:8080` (это имя сервиса только внутри docker-сети). UI всегда ходит на `/orders`, а Vite проксирует на backend.

UI живёт в папке `frontend/` и ходит в backend по REST (`/orders`).

### Запуск вместе с backend через Docker Compose
```bash
docker compose up --build
```

- Backend: http://localhost:8080
- UI: http://localhost:5173

### Локально (UI отдельно)
```bash
cd frontend
npm install
npm run dev
```

Если backend на другом хосте/порту, задайте:
```bash
VITE_API_URL=http://localhost:8080 npm run dev
```


---

## Дополнительно для UI: список заказов

UI использует эндпоинт:

- `GET /orders` — список (id, status, total, zone)

Пример:
```bash
curl -s http://localhost:8080/orders
```


---

## Event Storming → Code demo (новое для семинара)

В проект добавлен отдельный демонстрационный API, который показывает переход от стикеров Event Storming к коду:

- **Command** → входной use case / controller endpoint
- **Aggregate** → `domain/model/Order.java`
- **Domain Event** → `domain/event/*`
- **Policy** → `application/service/policy/*`
- **Read Model** → `application/port/in/result/OrderBoardReadModel.java`

### Упрощённый процесс для семинара

```text
CreateOrder      -> OrderCreated      -> Policy: ReceivePayment
ReceivePayment   -> PaymentReceived   -> Policy: StartCooking
StartCooking     -> CookingStarted    -> Policy: AssignCourier
AssignCourier    -> CourierAssigned   -> Policy: DeliverOrder
DeliverOrder     -> OrderDelivered
```

### Seminar API

Создать заказ и сразу увидеть событие + policy:
```bash
curl -s -X POST http://localhost:8080/seminar/workflow/orders   -H 'Content-Type: application/json'   -d '{
    "address": {"city":"Amsterdam","street":"Damrak","house":"1","apartment":"12","postcode":"1012"},
    "items": [
      {"name":"Margherita","size":"MEDIUM","qty":2},
      {"name":"Pepperoni","size":"LARGE","qty":1}
    ]
  }'
```

Подтвердить оплату:
```bash
curl -s -X POST http://localhost:8080/seminar/workflow/orders/<ID>/payment-received
```

Запустить кухню:
```bash
curl -s -X POST http://localhost:8080/seminar/workflow/orders/<ID>/start-cooking
```

Назначить курьера:
```bash
curl -s -X POST http://localhost:8080/seminar/workflow/orders/<ID>/assign-courier
```

Завершить доставку:
```bash
curl -s -X POST http://localhost:8080/seminar/workflow/orders/<ID>/deliver
```

Прочитать board/read model:
```bash
curl -s http://localhost:8080/seminar/workflow/orders/<ID>/board
```
