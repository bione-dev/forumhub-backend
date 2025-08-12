package com.challenge.AluraForum.dto;

import com.challenge.AluraForum.domain.Topico;

public record TopicoResponse(
        Long id, String titulo, String mensagem,
        String estado, String autor, String curso, String dataCriacao
) {
    public static TopicoResponse from(Topico t) {
        return new TopicoResponse(
                t.getId(), t.getTitulo(), t.getMensagem(),
                t.getEstado(), t.getAutor(), t.getCurso(),
                t.getDataCriacao().toString()
        );
    }
}
