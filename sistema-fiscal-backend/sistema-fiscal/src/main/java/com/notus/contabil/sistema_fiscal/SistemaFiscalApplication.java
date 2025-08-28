package com.notus.contabil.sistema_fiscal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling; // ✅ 1. IMPORT NECESSÁRIO

// ✅ 2. ADICIONE ESTA ANOTAÇÃO
@EnableScheduling 
@SpringBootApplication
public class SistemaFiscalApplication {

    public static void main(String[] args) {
        SpringApplication.run(SistemaFiscalApplication.class, args);
    }
}