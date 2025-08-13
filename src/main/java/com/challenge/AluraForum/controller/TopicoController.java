package com.challenge.AluraForum.controller;

import com.challenge.AluraForum.domain.Topico;
import com.challenge.AluraForum.dto.AtualizarTopicoRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/topicos")
@RequiredArgsConstructor
public class TopicoController {

    private final TopicoRepository repo;

    // Helpers para manter o tipo do ResponseEntity<TopicoResponse>
    private static <T> ResponseEntity<T> conflict() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
    }
    private static <T> ResponseEntity<T> notFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @PostMapping
    @Transactional
    public ResponseEntity<TopicoResponse> criar(@RequestBody @Valid NovoTopicoRequest req,
                                                UriComponentsBuilder uriBuilder) {
        if (repo.existsByTituloAndMensagem(req.titulo(), req.mensagem())) {
            return conflict();
        }

        Topico t = Topico.builder()
                .titulo(req.titulo())
                .mensagem(req.mensagem())
                .autor(req.autor())
                .curso(req.curso())
                .build();

        t = repo.save(t);
        var location = uriBuilder.path("/topicos/{id}").buildAndExpand(t.getId()).toUri();
        return ResponseEntity.created(location).body(TopicoResponse.from(t));
    }

    // LISTAGEM
    @GetMapping
    public ResponseEntity<Page<TopicoResponse>> listar(
            @RequestParam(required = false) String curso,
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false, defaultValue = "false") boolean top10,
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
            fim = inicio.plusYears(1);
        }

        Page<Topico> page = repo.search(curso, inicio, fim, pageable);
        return ResponseEntity.ok(page.map(TopicoResponse::from));
    }

    // DETALHAMENTO
    @GetMapping("/{id}")
    public ResponseEntity<TopicoResponse> detalhar(@PathVariable Long id) {
        return repo.findById(id)
                .map(t -> ResponseEntity.ok(TopicoResponse.from(t)))
                .orElseGet(TopicoController::notFound);
    }

    // ATUALIZAÇÃO
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<TopicoResponse> atualizar(@PathVariable Long id,
                                                    @RequestBody @Valid AtualizarTopicoRequest req) {
        return repo.findById(id).map(t -> {
            if (repo.existsByTituloAndMensagemAndIdNot(req.titulo(), req.mensagem(), id)) {
                // força o tipo do body para TopicoResponse
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body((TopicoResponse) null);
            }

            t.setTitulo(req.titulo());
            t.setMensagem(req.mensagem());
            t.setAutor(req.autor());
            t.setCurso(req.curso());
            t = repo.save(t);

            return ResponseEntity.ok(TopicoResponse.from(t));
        }).orElseGet(() ->
                ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body((TopicoResponse) null)
        );
    }

    // EXCLUSÃO
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        // evita EmptyResultDataAccessException do deleteById
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
