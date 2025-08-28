package com.notus.contabil.sistema_fiscal;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tarefas-modelo")
public class TarefaModeloController {

    @Autowired
    private TarefaModeloService tarefaModeloService;

    @GetMapping
    public ResponseEntity<List<TarefaModeloDTO>> listarTarefasModelo() {
        return ResponseEntity.ok(tarefaModeloService.listarTodos());
    }

    @PostMapping
    public ResponseEntity<TarefaModeloDTO> criarTarefaModelo(@Valid @RequestBody TarefaModeloRequestDTO dto) {
        TarefaModeloDTO novaTarefa = tarefaModeloService.criar(dto);
        return ResponseEntity.status(201).body(novaTarefa);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TarefaModeloDTO> atualizarTarefaModelo(@PathVariable Long id, @Valid @RequestBody TarefaModeloRequestDTO dto) {
        return tarefaModeloService.atualizar(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarTarefaModelo(@PathVariable Long id) {
        if (tarefaModeloService.deletar(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}