package com.notus.contabil.sistema_fiscal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Esta única anotação configura tudo automaticamente!
@SpringBootApplication
public class SistemaFiscalApplication {

	// Este é o novo "main" que inicia toda a aplicação web
	public static void main(String[] args) {
		SpringApplication.run(SistemaFiscalApplication.class, args);
	}

}