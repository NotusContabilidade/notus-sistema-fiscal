package com.notus.contabil.sistema_fiscal;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:5173") 
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final DatabaseManager dbManager = new DatabaseManager();

    public record ClienteDashboardDTO(DatabaseManager.Cliente cliente, DatabaseManager.ParametrosSN parametros) {}

    @GetMapping
    public ResponseEntity<?> buscarClientePorCnpj(@RequestParam String cnpj) {
        String cnpjLimpo = cnpj.replaceAll("[^0-9]", "");

        Optional<DatabaseManager.Cliente> clienteOpt = dbManager.clienteDAO.findByCnpj(cnpjLimpo);

        if (clienteOpt.isPresent()) {
            DatabaseManager.Cliente cliente = clienteOpt.get();
            Optional<DatabaseManager.ParametrosSN> parametrosOpt = dbManager.parametrosSNDAO.findByClienteId(cliente.id());
            
            if (parametrosOpt.isPresent()) {
                return ResponseEntity.ok(new ClienteDashboardDTO(cliente, parametrosOpt.get()));
            }
        }
        
        return ResponseEntity.notFound().build();
    }

    public record NovoClienteDTO(String cnpj, String razaoSocial, double rbt12, double folha12m) {}
    
    @PostMapping
    public ResponseEntity<DatabaseManager.Cliente> salvarCliente(@RequestBody NovoClienteDTO novoClienteDTO) {
        try {
            String cnpjLimpo = novoClienteDTO.cnpj().replaceAll("[^0-g]", "");
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

    // --- NOVO ENDPOINT PARA ATUALIZAR PARÂMETROS ---
    public record ParametrosUpdateDTO(double rbt12, double folha12m) {}

    @PutMapping("/{clienteId}/parametros")
    public ResponseEntity<?> updateParametros(@PathVariable Long clienteId, @RequestBody ParametrosUpdateDTO dto) {
        try {
            dbManager.parametrosSNDAO.updateByClienteId(clienteId, dto.rbt12(), dto.folha12m());
            return ResponseEntity.ok().build(); // Retorna 200 OK se for bem-sucedido
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    // ADICIONE ESTE NOVO MÉTODO DENTRO DA CLASSE ClienteController

    @GetMapping("/id/{id}")
    public ResponseEntity<?> buscarClientePorId(@PathVariable Long id) {
        Optional<DatabaseManager.Cliente> clienteOpt = dbManager.clienteDAO.findById(id);

        if (clienteOpt.isPresent()) {
            DatabaseManager.Cliente cliente = clienteOpt.get();
            Optional<DatabaseManager.ParametrosSN> parametrosOpt = dbManager.parametrosSNDAO.findByClienteId(cliente.id());
            
            // Retorna o mesmo DTO do dashboard, garantindo consistência
            if (parametrosOpt.isPresent()) {
                return ResponseEntity.ok(new ClienteDashboardDTO(cliente, parametrosOpt.get()));
            }
        }
        
        return ResponseEntity.notFound().build();
    }
}