package com.challenge.AluraForum.repository;

import com.challenge.AluraForum.domain.UserSession;
import com.challenge.AluraForum.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    Optional<UserSession> findByUsuario_Login(String login);
    Optional<UserSession> findByUsuario(Usuario usuario);
}
