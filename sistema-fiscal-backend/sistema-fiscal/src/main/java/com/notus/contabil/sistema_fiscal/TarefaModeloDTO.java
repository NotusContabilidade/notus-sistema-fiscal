package com.notus.contabil.sistema_fiscal;

import java.util.List;

/**
 * DTO para representar uma TarefaModelo ao ser enviada para o cliente (frontend).
 */
public record TarefaModeloDTO(
    Long id,
    String titulo,
    String descricao,
    String departamento,
    int diasParaCriacaoAntecipada,
    int diaVencimentoMes,
    List<String> checklist
) {
    // Construtor estático para facilitar a conversão da Entidade para DTO
    public static TarefaModeloDTO fromEntity(TarefaModelo entity) {
        return new TarefaModeloDTO(
            entity.getId(),
            entity.getTitulo(),
            entity.getDescricao(),
            entity.getDepartamento(),
            entity.getDiasParaCriacaoAntecipada(),
            entity.getDiaVencimentoMes(),
            entity.getChecklist()
        );
    }
}