package com.notus.contabil.sistema_fiscal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling; // <-- IMPORTE

@SpringBootApplication
@EnableScheduling // <-- ADICIONE ESTA ANOTAÇÃO
public class SistemaFiscalApplication {

    public static void main(String[] args) {
        SpringApplication.run(SistemaFiscalApplication.class, args);
    }

}