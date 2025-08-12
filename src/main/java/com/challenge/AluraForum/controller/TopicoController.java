package com.challenge.AluraForum.controller;



import com.challenge.AluraForum.domain.Topico;
import com.challenge.AluraForum.dto.NovoTopicoRequest;
import com.challenge.AluraForum.dto.TopicoResponse;
import com.challenge.AluraForum.repository.TopicoRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

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
}


