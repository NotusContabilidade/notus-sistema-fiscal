package com.notus.contabil.sistema_fiscal;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:5173") 
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final DatabaseManager dbManager = new DatabaseManager();

    // Novo DTO para enviar todos os dados do dashboard de uma só vez
    public record ClienteDashboardDTO(DatabaseManager.Cliente cliente, DatabaseManager.ParametrosSN parametros) {}

    @GetMapping
    public ResponseEntity<?> buscarClientePorCnpj(@RequestParam String cnpj) {
        String cnpjLimpo = cnpj.replaceAll("[^0-9]", "");

        Optional<DatabaseManager.Cliente> clienteOpt = dbManager.clienteDAO.findByCnpj(cnpjLimpo);

        if (clienteOpt.isPresent()) {
            DatabaseManager.Cliente cliente = clienteOpt.get();
            // Busca também os parâmetros fiscais
            Optional<DatabaseManager.ParametrosSN> parametrosOpt = dbManager.parametrosSNDAO.findByClienteId(cliente.id());
            
            if (parametrosOpt.isPresent()) {
                // Se encontrar tudo, envia o DTO completo
                return ResponseEntity.ok(new ClienteDashboardDTO(cliente, parametrosOpt.get()));
            }
        }
        
        // Se não encontrar o cliente ou os parâmetros, retorna Not Found
        return ResponseEntity.notFound().build();
    }

    // O endpoint de salvar cliente permanece o mesmo
    public record NovoClienteDTO(String cnpj, String razaoSocial, double rbt12, double folha12m) {}
    @PostMapping
    public ResponseEntity<DatabaseManager.Cliente> salvarCliente(@RequestBody NovoClienteDTO novoClienteDTO) {
        // ... (código sem alterações)
        try {
            String cnpjLimpo = novoClienteDTO.cnpj().replaceAll("[^0-9]", "");
            if (dbManager.clienteDAO.findByCnpj(cnpjLimpo).isPresent()) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.CONFLICT).build();
            }
            DatabaseManager.Cliente clienteSalvo = dbManager.clienteDAO.save(cnpjLimpo, novoClienteDTO.razaoSocial());
            dbManager.parametrosSNDAO.save(clienteSalvo.id(), novoClienteDTO.rbt12(), novoClienteDTO.folha12m());
            return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(clienteSalvo);
        } catch (Exception e) {
            e.printStackTrace(); 
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
