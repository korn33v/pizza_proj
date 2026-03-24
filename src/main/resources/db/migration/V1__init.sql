-- [SEMINAR-17] Flyway: схема БД управляется миграциями, а не "hibernate ddl-auto=create".
-- Это помогает держать историю, проверять изменения в PR и воспроизводить окружения.

CREATE TABLE orders (
  id UUID PRIMARY KEY,
  city VARCHAR(100) NOT NULL,
  street VARCHAR(200) NOT NULL,
  house VARCHAR(50) NOT NULL,
  apartment VARCHAR(50),
  postcode VARCHAR(20) NOT NULL,

  status VARCHAR(30) NOT NULL,
  delivery_zone VARCHAR(50) NOT NULL,

  total_amount NUMERIC(19,2) NOT NULL,
  total_currency VARCHAR(3) NOT NULL,

  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE order_items (
  id BIGSERIAL PRIMARY KEY,
  order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
  name VARCHAR(120) NOT NULL,
  size VARCHAR(20) NOT NULL,
  qty INT NOT NULL
);

CREATE INDEX idx_order_items_order_id ON order_items(order_id);
