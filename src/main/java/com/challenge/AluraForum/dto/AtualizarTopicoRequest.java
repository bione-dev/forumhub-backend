package com.challenge.AluraForum.dto;

import jakarta.validation.constraints.NotBlank;


    public record AtualizarTopicoRequest(
            @NotBlank String titulo,
            @NotBlank String mensagem,
            @NotBlank String autor,
            @NotBlank String curso
    ) {}
