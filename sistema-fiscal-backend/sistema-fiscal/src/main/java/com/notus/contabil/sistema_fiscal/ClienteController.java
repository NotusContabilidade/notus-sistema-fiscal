package com.notus.contabil.sistema_fiscal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.Optional;

// Adicionado para permitir requisições do frontend React
@CrossOrigin(origins = "http://localhost:5173") 
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final DatabaseManager dbManager = new DatabaseManager();

    @GetMapping
    public ResponseEntity<?> buscarClientePorCnpj(@RequestParam String cnpj) {
        String cnpjLimpo = cnpj.replaceAll("[^0-9]", "");

        Optional<DatabaseManager.Cliente> clienteOpt = dbManager.clienteDAO.findByCnpj(cnpjLimpo);

        if (clienteOpt.isPresent()) {
            return ResponseEntity.ok(clienteOpt.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    public record NovoClienteDTO(String cnpj, String razaoSocial, double rbt12, double folha12m) {}

    @PostMapping
    public ResponseEntity<DatabaseManager.Cliente> salvarCliente(@RequestBody NovoClienteDTO novoClienteDTO) {
        try {
            String cnpjLimpo = novoClienteDTO.cnpj().replaceAll("[^0-9]", "");

            if (dbManager.clienteDAO.findByCnpj(cnpjLimpo).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            DatabaseManager.Cliente clienteSalvo = dbManager.clienteDAO.save(cnpjLimpo, novoClienteDTO.razaoSocial());
            dbManager.parametrosSNDAO.save(clienteSalvo.id(), novoClienteDTO.rbt12(), novoClienteDTO.folha12m());

            return ResponseEntity.status(HttpStatus.CREATED).body(clienteSalvo);
        } catch (Exception e) {
            e.printStackTrace(); 
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}