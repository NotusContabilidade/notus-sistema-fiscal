package com.notus.contabil.sistema_fiscal.dto;

import java.time.LocalDateTime;

// Usando 'record' para um DTO simples e imutável
public record ComentarioDTO(
    Long id,
    Long taskId,
    String autor,
    String texto,
    LocalDateTime dataCriacao
) {}