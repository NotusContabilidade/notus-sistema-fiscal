package com.notus.contabil.sistema_fiscal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/escritorios")
public class EscritorioController {

    @Autowired
    private EscritorioService escritorioService;

    // DTO para receber os dados de cadastro
    public record CadastroRequest(String razaoSocial, String cnpj) {}

    @PostMapping("/cadastrar")
    public ResponseEntity<Escritorio> cadastrar(@RequestBody CadastroRequest request) {
        try {
            Escritorio novoEscritorio = escritorioService.cadastrarNovoEscritorio(
                request.razaoSocial(), 
                request.cnpj()
            );
            return new ResponseEntity<>(novoEscritorio, HttpStatus.CREATED);
        } catch (Exception e) {
            // Em uma aplicação real, teríamos um tratamento de erro mais robusto
            return ResponseEntity.internalServerError().build();
        }
    }
}