package com.challenge.AluraForum.controller;



import com.challenge.AluraForum.domain.Topico;
import com.challenge.AluraForum.dto.NovoTopicoRequest;
import com.challenge.AluraForum.dto.TopicoResponse;
import com.challenge.AluraForum.repository.TopicoRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/topicos")
@RequiredArgsConstructor
public class TopicoController {

    private final TopicoRepository repo;

    @PostMapping
    @Transactional
    public ResponseEntity<TopicoResponse> criar(@RequestBody @Valid NovoTopicoRequest req,
                                                UriComponentsBuilder uriBuilder) {
        if (repo.existsByTituloAndMensagem(req.titulo(), req.mensagem())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // 409 duplicado
        }

        Topico t = Topico.builder()
                .titulo(req.titulo())
                .mensagem(req.mensagem())
                .autor(req.autor())
                .curso(req.curso())
                .build();

        t = repo.save(t);

        var location = uriBuilder.path("/topicos/{id}").buildAndExpand(t.getId()).toUri();
        return ResponseEntity.created(location).body(TopicoResponse.from(t)); // 201
    }
    // LISTAGEM DE TÓPICOS (com filtros, top10 e paginação)
    @GetMapping
    public ResponseEntity<Page<TopicoResponse>> listar(
            @RequestParam(required = false) String curso,           // filtro por nome do curso (contains, case-insensitive)
            @RequestParam(required = false) Integer ano,            // filtro por ano de criação
            @RequestParam(required = false, defaultValue = "false") boolean top10, // opcional: top 10 por data ASC
            @PageableDefault(sort = "dataCriacao", direction = Sort.Direction.ASC, size = 20)
            Pageable pageable
    ) {
        if (top10) {
            pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "dataCriacao"));
        }

        LocalDateTime inicio = null;
        LocalDateTime fim = null;
        if (ano != null) {
            inicio = LocalDate.of(ano, 1, 1).atStartOfDay();
            fim = inicio.plusYears(1); // [ano-01-01T00:00, próximo ano)
        }

        Page<Topico> page = repo.search(curso, inicio, fim, pageable);
        return ResponseEntity.ok(page.map(TopicoResponse::from));
    }
}


