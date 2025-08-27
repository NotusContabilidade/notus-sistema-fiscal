package com.notus.contabil.sistema_fiscal;

import java.math.BigDecimal;

// Agora o DTO é uma classe pública de alto nível em seu próprio arquivo.
public record ValoresExtraidosDTO(BigDecimal comRetencao, BigDecimal semRetencao) {}