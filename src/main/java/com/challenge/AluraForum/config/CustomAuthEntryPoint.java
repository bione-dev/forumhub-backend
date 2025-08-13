package com.challenge.AluraForum.config;

import com.challenge.AluraForum.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        String json = """
        {
          "status": 401,
          "error": "Unauthorized",
          "message": "Você precisa estar autenticado para acessar este recurso.",
          "path": "%s"
        }
        """.formatted(request.getRequestURI());

        response.getWriter().write(json);
    }
}


