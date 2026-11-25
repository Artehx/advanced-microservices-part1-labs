-- Training only. Schemas represent logical ownership; not physical isolation.
CREATE SCHEMA orders;
CREATE SCHEMA inventory;
CREATE SCHEMA payments;
CREATE TABLE orders.stream (id text PRIMARY KEY);
CREATE TABLE orders.event (
  order_id text NOT NULL REFERENCES orders.stream(id),
  version integer NOT NULL CHECK (version > 0),
  kind text NOT NULL,
  sku text NOT NULL,
  quantity integer NOT NULL CHECK (quantity > 0),
  decline boolean NOT NULL,
  PRIMARY KEY (order_id, version)
);
CREATE TABLE orders.read_model (
  order_id text PRIMARY KEY,
  sku text NOT NULL,
  quantity integer NOT NULL,
  status text NOT NULL,
  version integer NOT NULL
);
CREATE TABLE inventory.stock (
  sku text PRIMARY KEY, available integer NOT NULL CHECK (available >= 0)
);
INSERT INTO inventory.stock VALUES ('SKU-001',42),('SKU-002',7),('SKU-003',0);
CREATE TABLE inventory.reservation (
  order_id text PRIMARY KEY, sku text NOT NULL, quantity integer NOT NULL CHECK(quantity > 0),
  status text NOT NULL CHECK(status IN ('RESERVED','RELEASED','REJECTED'))
);
CREATE TABLE payments.decision (
  order_id text PRIMARY KEY, approved boolean NOT NULL
);
