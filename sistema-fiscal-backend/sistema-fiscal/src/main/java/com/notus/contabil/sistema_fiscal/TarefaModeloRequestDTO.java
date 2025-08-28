package com.notus.contabil.sistema_fiscal;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * DTO para receber dados do cliente (frontend) para criar ou atualizar uma TarefaModelo.
 * Inclui validações para garantir a integridade dos dados.
 */
public record TarefaModeloRequestDTO(
    @NotBlank(message = "O título não pode ser vazio.")
    String titulo,
    
    String descricao,
    
    String departamento,
    
    @NotNull(message = "Os dias de antecendência são obrigatórios.")
    @Min(value = 1, message = "A antecendência mínima é de 1 dia.")
    Integer diasParaCriacaoAntecipada,
    
    @NotNull(message = "O dia de vencimento é obrigatório.")
    @Min(value = 1, message = "O dia do vencimento deve ser no mínimo 1.")
    @Max(value = 31, message = "O dia do vencimento deve ser no máximo 31.")
    Integer diaVencimentoMes,
    
    List<String> checklist
) {}