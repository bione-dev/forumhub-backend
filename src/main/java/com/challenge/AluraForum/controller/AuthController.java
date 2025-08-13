package com.challenge.AluraForum.controller;

import com.challenge.AluraForum.domain.Usuario;
import com.challenge.AluraForum.domain.UserSession;
import com.challenge.AluraForum.dto.LoginRequest;
import com.challenge.AluraForum.dto.NovoUsuarioRequest;
import com.challenge.AluraForum.dto.TokenResponse;
import com.challenge.AluraForum.enums.Role;
import com.challenge.AluraForum.exception.ForbiddenException;
import com.challenge.AluraForum.exception.UnauthorizedException;
import com.challenge.AluraForum.repository.UsuarioRepository;
import com.challenge.AluraForum.repository.UserSessionRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.challenge.AluraForum.config.JwtService;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final PasswordEncoder encoder;
    private final UsuarioRepository usuarioRepository;
    private final UserSessionRepository userSessionRepository;

    @PostMapping("/login")
    public TokenResponse login(@RequestBody @Valid LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.login(), req.senha())
        );

        var user = (UserDetails) auth.getPrincipal();
        var usuario = usuarioRepository.findByLogin(user.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Gerar token e salvar na sessão
        String token = jwtService.gerarToken(usuario.getLogin(), String.valueOf(usuario.getId()));

        userSessionRepository.findByUsuario(usuario).ifPresentOrElse(
                sess -> {
                    sess.setUltimoToken(token);
                    sess.setDataGeracao(LocalDateTime.now());
                    userSessionRepository.save(sess);
                },
                () -> {
                    userSessionRepository.save(UserSession.builder()
                            .usuario(usuario)
                            .ultimoToken(token)
                            .dataGeracao(LocalDateTime.now())
                            .build());
                }
        );

        return new TokenResponse(token, "Bearer");
    }

    @PostMapping("/criar-usuario")
    public Usuario criar(@RequestBody @Valid NovoUsuarioRequest req) {
        Role roleFinal;

        if (req.role() == null || req.role().isBlank()) {
            roleFinal = Role.USER;
        } else {
            try {
                roleFinal = Role.valueOf(req.role().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Role inválida. Use ADMIN ou USER.");
            }
        }

        if (roleFinal == Role.ADMIN) {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                throw new UnauthorizedException("É necessário estar logado para criar um ADMIN.");
            }
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin) {
                throw new ForbiddenException("Apenas usuários ADMIN podem criar outro ADMIN.");
            }
        }

        Usuario usuario = new Usuario();
        usuario.setLogin(req.login());
        usuario.setSenha(encoder.encode(req.senha()));
        usuario.setRole(roleFinal);

        return usuarioRepository.save(usuario);
    }




}
