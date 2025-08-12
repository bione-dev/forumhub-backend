-- Unicidade (titulo+mensagem) sem indexar TEXT direto
ALTER TABLE topico
  ADD COLUMN titulo_mensagem_hash BINARY(32)
    GENERATED ALWAYS AS (UNHEX(SHA2(CONCAT(titulo, '\0', mensagem), 256))) STORED;

CREATE UNIQUE INDEX uk_topico_titulo_mensagem
  ON topico (titulo_mensagem_hash);