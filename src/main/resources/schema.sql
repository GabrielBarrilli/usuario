-- Schema bootstrap idempotente: cria só o que não existe e nunca remove dados.

CREATE TABLE IF NOT EXISTS usuario (
    id    BIGSERIAL PRIMARY KEY,
    name  VARCHAR(100),
    email VARCHAR(100) UNIQUE NOT NULL,
    senha VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS endereco (
    id           BIGSERIAL PRIMARY KEY,
    rua          VARCHAR(255),
    numero       BIGINT,
    complemento  VARCHAR(30),
    cidade       VARCHAR(150),
    estado       VARCHAR(2),
    cep          VARCHAR(9),
    usuario_id   BIGINT REFERENCES usuario (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS telefone (
    id          BIGSERIAL PRIMARY KEY,
    numero      VARCHAR(10),
    ddd         VARCHAR(3),
    usuario_id  BIGINT REFERENCES usuario (id) ON DELETE CASCADE
);
