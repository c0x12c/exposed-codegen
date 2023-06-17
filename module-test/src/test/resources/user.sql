CREATE TABLE IF NOT EXISTS users
(
  id         uuid DEFAULT uuid_generate_v4() NOT NULL,
  name       TEXT      NOT NULL,
  age        INTEGER   NOT NULL,
  bio        TEXT,
  platform   TEXT      NOT NULL,
  updated_at TIMESTAMP,
  deleted_at TIMESTAMP,
  created_at TIMESTAMP NOT NULL
)