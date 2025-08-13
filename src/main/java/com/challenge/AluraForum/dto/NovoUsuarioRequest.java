package com.challenge.AluraForum.dto;

import jakarta.validation.constraints.NotBlank;

public record NovoUsuarioRequest(
        @NotBlank String login,
        @NotBlank String senha,
        String role // pode ser null ou ""
) {}
