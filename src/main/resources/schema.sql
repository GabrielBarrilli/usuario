-- Schema bootstrap que nao remove dados existentes.
-- Executa so na primeira vez: se a tabela principal ja existe, o script sai sem tocar em nada.

DO $body$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = current_schema()
          AND table_name = 'usuario'
    ) THEN
        RAISE NOTICE 'schema.sql skipped (tables already created)';
        RETURN;
    END IF;

    -- Tabela usuario
    CREATE TABLE usuario (
        id    BIGSERIAL PRIMARY KEY,
        name  VARCHAR(100),
        email VARCHAR(100) UNIQUE NOT NULL,
        senha VARCHAR(255)
    );

    CREATE UNIQUE INDEX uk_usuario_email ON usuario (email);

    -- Tabela endereco
    CREATE TABLE endereco (
        id           BIGSERIAL PRIMARY KEY,
        rua          VARCHAR(255),
        numero       BIGINT,
        complemento  VARCHAR(30),
        cidade       VARCHAR(150),
        estado       VARCHAR(2),
        cep          VARCHAR(9),
        usuario_id   BIGINT
    );

    ALTER TABLE endereco
        ADD CONSTRAINT fk_endereco_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE;

    -- Tabela telefone
    CREATE TABLE telefone (
        id          BIGSERIAL PRIMARY KEY,
        numero      VARCHAR(10),
        ddd         VARCHAR(3),
        usuario_id  BIGINT
    );

    ALTER TABLE telefone
        ADD CONSTRAINT fk_telefone_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE;
END
$body$;
