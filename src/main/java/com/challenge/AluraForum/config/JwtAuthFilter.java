package com.challenge.AluraForum.config;

import com.challenge.AluraForum.exception.UnauthorizedException;
import com.challenge.AluraForum.repository.UserSessionRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserSessionRepository userSessionRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                String login = jwtService.validarEGetSubject(token);
                var sessionOpt = userSessionRepository.findByUsuario_Login(login);
                if (sessionOpt.isPresent() && !token.equals(sessionOpt.get().getUltimoToken())) {
                    throw new UnauthorizedException("Token não é o mais recente.");
                }

                var user = userDetailsService.loadUserByUsername(login);
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities())
                );

            } catch (UnauthorizedException e) {
                throw e;
            } catch (Exception e) {
                e.printStackTrace(); // loga mas não derruba
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        String path = req.getServletPath();
        System.out.println("shouldNotFilter path: " + path);

        return path.equals("/auth/login")
                || "OPTIONS".equalsIgnoreCase(req.getMethod())
                || path.startsWith("/error")
                || path.equals("/favicon.ico");
    }



}
