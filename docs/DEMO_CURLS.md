# Curl-команды для преподавательского демо

## 1. Создать заказ
```bash
curl -s -X POST http://localhost:8080/seminar/workflow/orders   -H 'Content-Type: application/json'   -d '{
    "address": {"city":"Amsterdam","street":"Damrak","house":"1","apartment":"12","postcode":"1012"},
    "items": [
      {"name":"Margherita","size":"MEDIUM","qty":2},
      {"name":"Pepperoni","size":"LARGE","qty":1}
    ]
  }'
```

## 2. Принять оплату
```bash
curl -s -X POST http://localhost:8080/seminar/workflow/orders/<ID>/payment-received
```

## 3. Запустить кухню
```bash
curl -s -X POST http://localhost:8080/seminar/workflow/orders/<ID>/start-cooking
```

## 4. Назначить курьера
```bash
curl -s -X POST http://localhost:8080/seminar/workflow/orders/<ID>/assign-courier
```

## 5. Завершить доставку
```bash
curl -s -X POST http://localhost:8080/seminar/workflow/orders/<ID>/deliver
```

## 6. Прочитать board view
```bash
curl -s http://localhost:8080/seminar/workflow/orders/<ID>/board
```
