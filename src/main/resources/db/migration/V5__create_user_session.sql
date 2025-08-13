CREATE TABLE user_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    ultimo_token VARCHAR(512) NOT NULL,
    data_geracao TIMESTAMP NOT NULL,
    CONSTRAINT fk_user_session_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);
