package com.notus.contabil.sistema_fiscal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController; // 👈 MUDOU AQUI

@RestController // 👈 MUDOU DE @Controller PARA @RestController
public class HomeController {

    @GetMapping("/")
    public String paginaInicial() {
        // Agora retorna o texto diretamente como resposta da API
        return "Bem-vindo à API do Nótus Sistema Fiscal!";
    }
}