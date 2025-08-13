package com.challenge.AluraForum.repository;

import com.challenge.AluraForum.domain.Topico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface TopicoRepository extends JpaRepository<Topico, Long> {
    boolean existsByTituloAndMensagem(String titulo, String mensagem);
    boolean existsByTituloAndMensagemAndIdNot(String titulo, String mensagem, Long id);

    @Query("""
           SELECT t FROM Topico t
           WHERE (:curso IS NULL OR LOWER(t.curso) LIKE LOWER(CONCAT('%', :curso, '%')))
             AND (:inicio IS NULL OR t.dataCriacao >= :inicio)
             AND (:fim IS NULL OR t.dataCriacao < :fim)
           """)
    Page<Topico> search(@Param("curso") String curso,
                        @Param("inicio") LocalDateTime inicio,
                        @Param("fim") LocalDateTime fim,
                        Pageable pageable);
}
