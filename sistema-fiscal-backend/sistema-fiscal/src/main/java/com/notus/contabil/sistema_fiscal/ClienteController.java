package com.notus.contabil.sistema_fiscal;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity; // Import necessário
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping; // Import necessário
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ParametrosSNRepository parametrosSNRepository;
    
    // DTOs existentes
    public record ClienteDashboardDTO(Cliente cliente, ParametrosSN parametros) {}
    public record NovoClienteDTO(String cnpj, String razaoSocial, double rbt12, double folha12m) {}
    public record ParametrosUpdateDTO(double rbt12, double folha12m) {}
    
    // ✅ NOVO DTO ADICIONADO PARA A LISTA DE CLIENTES
    public record ClienteListaDTO(Long id, String cnpj, String razaoSocial) {}

    @Transactional(readOnly = true)
    @GetMapping
    public ResponseEntity<?> buscarClientePorCnpj(@RequestParam String cnpj) {
        String cnpjLimpo = cnpj.replaceAll("[^0-9]", "");
        Optional<Cliente> clienteOpt = clienteRepository.findByCnpj(cnpjLimpo);
        if (clienteOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Cliente cliente = clienteOpt.get();
        Optional<ParametrosSN> parametrosOpt = parametrosSNRepository.findByClienteId(cliente.getId());
        return ResponseEntity.ok(new ClienteDashboardDTO(cliente, parametrosOpt.orElse(null)));
    }
    
    @Transactional(readOnly = true)
    @GetMapping("/id/{id}")
    public ResponseEntity<?> buscarClientePorId(@PathVariable Long id) {
        Optional<Cliente> clienteOpt = clienteRepository.findById(id);
        if (clienteOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Cliente cliente = clienteOpt.get();
        Optional<ParametrosSN> parametrosOpt = parametrosSNRepository.findByClienteId(id);
        return ResponseEntity.ok(new ClienteDashboardDTO(cliente, parametrosOpt.orElse(null)));
    }

    // ✅ NOVO ENDPOINT ADICIONADO PARA BUSCAR TODOS OS CLIENTES
    @GetMapping("/todos")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ClienteListaDTO>> getAllClientes() {
        List<ClienteListaDTO> listaClientes = clienteRepository.findAll().stream()
                .map(cliente -> new ClienteListaDTO(cliente.getId(), cliente.getCnpj(), cliente.getRazaoSocial()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(listaClientes);
    }

    @Transactional
    @PostMapping
    public ResponseEntity<?> salvarCliente(@RequestBody NovoClienteDTO novoClienteDTO) {
        String cnpjLimpo = novoClienteDTO.cnpj().replaceAll("[^0-9]", "");
        Cliente clienteParaSalvar = clienteRepository.findByCnpj(cnpjLimpo)
            .orElseGet(() -> {
                Cliente novoCliente = new Cliente();
                novoCliente.setCnpj(cnpjLimpo);
                novoCliente.setRazaoSocial(novoClienteDTO.razaoSocial());
                return clienteRepository.save(novoCliente);
            });
        if (parametrosSNRepository.findByClienteId(clienteParaSalvar.getId()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        ParametrosSN parametros = new ParametrosSN();
        parametros.setCliente(clienteParaSalvar);
        parametros.setRbt12Atual(novoClienteDTO.rbt12());
        parametros.setFolhaPagamento12mAtual(novoClienteDTO.folha12m());
        ParametrosSN parametrosSalvos = parametrosSNRepository.save(parametros);
        ClienteDashboardDTO dtoDeRetorno = new ClienteDashboardDTO(clienteParaSalvar, parametrosSalvos);
        return ResponseEntity.status(HttpStatus.CREATED).body(dtoDeRetorno);
    }
    
    @Transactional
    @PutMapping("/{clienteId}/parametros")
    public ResponseEntity<?> updateParametros(@PathVariable Long clienteId, @RequestBody ParametrosUpdateDTO dto) {
        Optional<Cliente> clienteOpt = clienteRepository.findById(clienteId);
        if (clienteOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        ParametrosSN parametros = parametrosSNRepository.findByClienteId(clienteId)
                .orElse(new ParametrosSN());
        parametros.setCliente(clienteOpt.get());
        parametros.setRbt12Atual(dto.rbt12());
        parametros.setFolhaPagamento12mAtual(dto.folha12m());
        parametrosSNRepository.save(parametros);
        return ResponseEntity.ok().build();
    }
}