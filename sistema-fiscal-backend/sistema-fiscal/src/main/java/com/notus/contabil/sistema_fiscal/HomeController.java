package com.notus.contabil.sistema_fiscal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller // 1. Anotação que transforma esta classe em um "controlador web"
public class HomeController {

    // 2. Este método será acionado quando alguém acessar a página inicial ("/")
    @GetMapping("/")
    public String paginaInicial(Model model) {
        
        // 3. Adiciona um "dado" que será enviado para o HTML
        model.addAttribute("mensagem", "Bem-vindo ao novo Sistema de Gestão Contábil!");
        
        // 4. Retorna o nome do arquivo HTML que deve ser exibido
        return "index"; // -> Isso vai procurar por um arquivo chamado 'index.html'
    }
}