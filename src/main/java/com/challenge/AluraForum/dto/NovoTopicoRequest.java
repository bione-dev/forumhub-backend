package com.challenge.AluraForum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NovoTopicoRequest(
        @NotBlank @Size(max = 200) String titulo,
        @NotBlank String mensagem,
        @NotBlank String autor,
        @NotBlank String curso
) {}
